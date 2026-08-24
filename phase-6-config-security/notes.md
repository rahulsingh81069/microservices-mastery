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
