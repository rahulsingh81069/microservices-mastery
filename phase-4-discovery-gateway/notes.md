# Phase 4: Service Discovery & API Gateway

## Goal

- Setup Eureka Server (Service Registry)
- Register user-service, product-service, order-service with Eureka
- Replace hardcoded URLs with service names
- Setup API Gateway (single entry point)

## Services in this phase

- eureka-server (Registry)
- api-gateway (Single entry point)
- user-service, product-service, order-service (registered clients)

## Yaad karo humara UserClient:
   > @FeignClient(name = "user-service", url = "http://localhost:8081")

    Yeh 3 real problems create karta hai:

    Problem 1: Scaling Ka Issue

        Socho Diwali sale chal rahi hai, aur User Service pe bahut load aa raha hai. Tumhe 3 copies (instances) of User Service chalani padengi load handle karne ke liye  jaise port 8081, 8091, 8101 pe. Ab Order Service kisko call kare? Hardcoded URL mein sirf ek hi address likh sakte ho.

    Problem 2: Address Change Hona

        Agar User Service ek naye server pe move ho (jo cloud mein normal hai), uska IP/port change ho sakta hai. Tumhe Order Service ka code manually change karke redeploy karna padega  yeh bahut fragile aur risky hai.

    Problem 3: Kaun Sa Service Available Hai, Pata Nahi Chalta

        Agar User Service ki 3 copies chal rahi hain aur ek crash ho jaye, Order Service ko kaise pata chalega kaunsi copy abhi zinda hai aur kaunsi nahi?

# Solution -> Service Registry (Eureka)
       
> Concept samjho ek real-life example se:

    Socho tum kisi naye shehar mein ho aur tumhe ek plumber chahiye. Tum Justdial/Google pe search karte ho "plumber near me"  woh tumhe available plumbers ki list deta hai unке current contact numbers ke saath. Tumhe khud har plumber ka number yaad nahi rakhna padta  ek central directory (Justdial) sabka pata rakhti hai.

> Service Registry (Eureka) bilkul yehi kaam karta hai microservices ke liye:

    Har service start hote hi khud ko Eureka mein register karti hai -> "Main User Service hoon, mera address yeh hai"

    Jab Order Service ko User Service se baat karni ho, woh Eureka se poochta hai -> "User Service kahan hai?" -> Eureka usko current, live address de deta hai

    Agar User Service ki multiple copies chal rahi hon, Eureka un सबको track karta hai aur load balance karne mein madad karta hai

> Agar Eureka na ho toh:

    Har service ka address hardcode aur manually manage karna padega
    Naya instance add karne ke liye saari doosri services ka code change karna padega
    Yeh bilkul non-scalable aur fragile approach hai -> real companies mein impossible hai isse manage karna jab 50-100 microservices ho 


