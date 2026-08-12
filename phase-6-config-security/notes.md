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
