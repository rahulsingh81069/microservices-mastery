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
