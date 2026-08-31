# Phase 6: Config Server & Security

## Goal

- Centralized Config Server - manage all services' config from one place
- Spring Security + JWT - authentication & authorization
- Understand OAuth2 basics

## Services in this phase

- config-server (new)
- eureka-server, api-gateway, user-service, product-service, order-service (updated to use config-server + security)

## Why this matters

Abhi tak har service ka apna alag application.properties tha.
Real companies mein 50+ services hain - har ek mein manually
config change karna impossible hai. Config Server ek central
jagah se sab services ka config manage karta hai.

Security - abhi tak koi bhi Postman se hamari APIs call kar sakta
hai bina kisi authentication ke. Real production mein yeh bahut
bada security risk hai.


Lesson 1: Config Server

Problem Samjho (Abhi Tak Kya Ho Raha Tha)

Abhi tak har service ka apna application.properties file uskी khud ki codebase mein tha. Agar kal tumhe database URL change karna ho, ya Eureka ka address update karna ho — tumhe:

Har service mein manually file edit karni padegi
Har service ko rebuild aur redeploy karna padega
Agar 50 services hon, yeh 50 baar karna padega — bahut error-prone aur time-consuming

Config Server bilkul yehi karta hai:

Saari services ka configuration ek central jagah (typically ek Git repository) mein rakha jata hा
Har service startup ke waqt Config Server se apna config fetch karti hai
Config change karna ho? Sirf Git repo update karo, services ko naya config automatically mil jata hai (with a refresh mechanism)

Config Server Kaise Kaam Karta Hai (Flow)

1. Tum apne config files (jaise user-service.properties) 
   ek alag GitHub repo mein daalte ho
        ↓
2. Config Server start hota hai, uska kaam hai 
   "us GitHub repo ko dekhna"
        ↓
3. Jab User Service start hoti hai, woh Config Server se poochti hai:
   "mera config kahan hai?"
        ↓
4. Config Server GitHub repo se woh file fetch karke 
   User Service ko de deta hai



   Yeh important hai samajhna: Config Server khud config store nahi karta — woh sirf ek middleman/fetcher hai jo Git repository (ya kabhi kabhi local files) se config read karke serve karta hai.

Agar Config Server Na Ho Toh Kya Problem Hai (Real Example)

Socho production mein User Service ka database password rotate karna hai (security practice). Bina Config Server ke:

Code mein password hardcoded/properties file mein hai
Password badalne ke liye naya build banao, naya deploy karo
Agar 10 services same database use kar rahi hain, 10 baar yeh karna padega


Sahi Startup Order (Ab Ka Naya Order)

1. Config Server (8888)   ← sabse pehle, kyunki sab isी pe depend karenge
2. Eureka Server (8761)
3. User Service, Product Service 
4. Order Service
5. API Gateway


import doubt :

# Q): agar config down raha tab service defult spring boot port 8080 pe chelaga what if same service already running on 8080 and config are down?


# Scenario Samjho

Socho:

API Gateway already port 8080 pe chal rahi hai
Config Server down hai

Tum User Service start karte ho, jisme optional:configserver:... hai

Kya hoga:

User Service Config Server se config lene ki koshish karegi
Config Server na milne pe, optional: ki wajah se crash nahi hogi

Koi explicit server.port na milne ki wajah se, Spring Boot default port 8080 use karne ki koshish karegi

Lekin 8080 already API Gateway use kar rahi hai — toh yahan conflict hoga
Iska Result Kya Hoga

Yeh bilkul wahi error hoga jo humne bahut pehle (Phase 4 mein) dekha tha:

Port 8080 was already in use

User Service start hi nahi hogi — application crash ho jayegi ek clear error ke saath "port already in use."

Important distinction samjho:

User Service crash nahi hui config missing hone ki wajah se (kyunki optional: tha)

User Service crash hui port conflict ki wajah se (jo ek downstream effect hai config missing hone ka)

Yeh do alag reasons hain, dono ek doosre se trigger hue.

Agar Port Conflict Na Ho (Jaise 8080 Khali Ho)

Toh User Service successfully start ho jayegi, lekin:

Port 8080 pe chalegi (galat, kyunki tumne 8081 chaha tha)
Database settings missing hongi (H2 apna default in-memory config use karega)
Eureka address missing hoga — matlab Eureka mein register hi nahi hogi, aur na hi discovery client kaam karega

Yeh sabse dangerous scenario hai — service chal rahi dikhegi (crash nahi hui), lekin poori tarah se broken/misconfigured hai, aur pehli nazar mein pata nahi chalega kya galat hai.

Real Industry Mein Iska Solution Kya Hai

Yeh exact reason hai ki production mein teams optional: hata dete hain critical services ke liye:

properties
spring.config.import=configserver:http://localhost:8888

(bina optional: ke) — taaki agar Config Server down ho, service explicitly, clearly crash ho ("Config Server unavailable" jaisा error), silently misconfigured hone ke bajaye. Yeh fail-fast principle ka hi ek aur example hai jo humne Phase 2 mein dekha tha (@Id missing hone par crash hona).

Golden rule: Silent misconfiguration (service chal rahi hai par galat) crash hone se zyada dangerous hota hai — kyunki crash turant dikhta hai, lekin silent misconfiguration production mein ghante/din baad pata chalta hai (jab tak koi customer complain na kare ya data corrupt na ho jaye).



# Phase 6, Lesson 2: Authentication Aur Authorization

# Pehle Do Terms Clear Karte Hain (Bahut Log Confuse Karte Hain)

Authentication — "Tum kaun ho?" (Identity verify karna) — jaise login karte waqt username/password check karna.

Authorization — "Tumhe kya karne ki permission hai?" (Access control) — jaise, login ho gaye ho, lekin kya tum "Admin" wale endpoints access kar sakte ho, ya sirf "User" wale?


Abhi Tak Hamari APIs Mein Kya Problem Hai

Abhi tak, koi bhi Postman khol ke tumhari /api/users, /api/orders APIs ko call kar sakta hai — bina kisi login, bina kisi identity ke. Yeh bahut bada security risk hai — socho agar yeh production mein ho, koi bhi anonymous user tumhara data dekh sakta hai, orders create kar sakta hai, kuch bhi kar sakta hai.

JWT (JSON Web Token) Kya Hai — Real-Life Analogy

Socho tum ek concert mein jaate ho. Entry gate pe tumhara ticket check hota hai (Authentication — "tum valid ho"), aur tumhe ek wristband mil jata hai. Ab poore concert mein, jaha bhi jao (VIP area, backstage, etc.), guard tumhara wristband dekh ke decide karta hai "tumhe yaha jaane ki permission hai ya nahi" (Authorization) — tumhe baar baar apna ticket/ID dikhane ki zarurat nahi, sirf wristband dikhana padta hai.

JWT bilkul yeh wristband hai:

1. Tum login karte ho (username/password bhejte ho)
2. Server verify karta hai, aur tumhe ek JWT token deta hai (ek encoded string)
3. Ab har request ke saath tum yeh token bhejte ho (header mein)
4. Server token dekh ke turant pehchan leta hai "yeh valid user hai," bina dobara database check  
   kiye baar baar.


# JWT Ke Andar Kya Hota Hai (High-Level)

# JWT token 3 parts se bana hota hai (dots se separated):

# header.payload.signature
Header — batata hai kaunsa encryption algorithm use hua
Payload — actual data (jaise userId, email, role, expiry time)
Signature — yeh confirm karta hai token tamper nahi hua (koi beech mein change nahi kar sakta bina signature invalid kiye)

Important: JWT encrypted nahi hota (koi bhi decode karke payload dekh sakta hai — sensitive data isme mat daalna), lekin woh signed hota hai — matlab koi usse change nahi kar sakta bina server ko pata chale.


# full Flow 

1. User login karta hai (username + password)
        ↓
2. Server verify karta hai credentials
        ↓
3. Server ek JWT token generate karta hai, user ko deta hai
        ↓
4. User har future request mein yeh token header mein bhejta hai:
   Authorization: Bearer <token>
        ↓
5. Server token verify karta hai (signature check), 
   agar valid hai, request process karta hai
        ↓
6. Agar token invalid/expired hai, "401 Unauthorized" milta hai


# now Code  Implement

add this to user-service pom.xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>


# Step 1: User Entity Update Karo

entity/User.java mein naye fields add karo:
private String password;
private String role; 


# Step 2: Password Encryption Samjho (Bahut Important)

Kabhi bhi password ko plain text mein database mein save nahi karte — yeh ek critical security rule hai. Agar database leak/hack ho jaye, saare passwords directly expose ho jayenge.

Solution: Password ko hash karke store karte hain, using BCrypt algorithm. BCrypt ek one-way function hai — matlab hash se original password wapas nikala nahi ja sakta (encryption se different hai, jo reversible hota hai).

Login Ke Waqt Kaise Verify Hota Hai (Agar Hash Se Wapas Nahi Nikal Sakte)

Jab user login karta hai:

1. Woh apna plain password bhejta hai (jaise "mypassword123")
2. Server usी algorithm se us plain password ko phir se hash karta hai
3. Naya hash aur database mein saved hash ko compare karta hai
4. Match ho toh login successful, warna fail


# step 3: Ab BCrypt Code Mein Implement Karte Hain

       step-1: config naam ka naya folder banao, usme SecurityConfig.java:

        @Configuration
        public class SecurityConfig {

                @Bean
                public PasswordEncoder passwordEncoder() {
                        return new BCryptPasswordEncoder();
                }
        }

        @Configuration — batata hai yeh class beans define karti hai (jaise @Service, @Repository, yeh bhi Spring ko bataने ka ek tarika hai "yaha kuch important define hua hai, isko manage karo").

        @Bean — yeh method Spring ko batata hai "iska return value ek managed object" hai — matlab tum ise @Autowired kar ke kahin bhi use kar sakte ho, bilkul jaise UserRepository ko karte the.


        Step 2: UserService.java Mein Password Hash Karo(createUser() method)

        **user.setPassword(passwordEncoder.encode(user.getPassword()));  // hash karo
          user.setRole("ROLE_USER");**

        passwordEncoder.encode(user.getPassword()) — yeh plain password (jaise "mypass123") ko BCrypt hash mein convert kar deta hai (jaise "$2a$10$N9qo8uLOickgx2ZMRZoMy...") — yehi hash database mein save hoga, plain password kahi nahi jayega.