# Phase 3: Service-to-Service Communication

## Goal

- Split User and Product into separate microservices (proper bounded context)
- Create Order Service that talks to both
- Learn synchronous communication between services (REST calls)
- Understand what happens when a dependent service is down

## Services in this phase

- user-service (own DB)
- product-service (own DB)
- order-service (calls user-service and product-service)


## User aur Product Ko Alag Services Kyun Banate Hain
    Abhi tak humne ek hi service (user-service) mein User aur Product dono rakhe the ,yeh sirf practice ke liye theek tha, lekin har service ek clear "bounded context" (business capability) ka owner honi chahiye.

    User aur Product yeh dono bilkul alag business concerns hain:

        - User ka data change hota hai jab profile update ho, password change ho
        - Product ka data change hota hai jab price update ho, stock update ho, naya product aaye

## Ab Naya Structure Kaisa Hoga:

    phase-3-communication/
├── user-service/       → Port 8081, apna database
├── product-service/     → Port 8082, apna database  
└── order-service/       → Port 8083, apna database (Order ka data), 
                            + calls user-service aur product-service

## Order Service Ka Kaam Kya Hoga (Real World Scenario Samjho)

    Socho ek customer order place karta hai:

         Order Service ko pehle check karna hai user valid hai ya nahi → isके liye woh User    Service ko call karega (jaise: "Yeh user ID 5 exist karta hai kya?")

        Order Service ko check karna hai product available hai ya nahi, aur stock hai ya nahi → isके liye woh Product Service ko call karega

        Dono confirm hone ke baad, Order Service apna khud ka order record save karega apni database mein

    -> Yeh call kaise hoti hai?

        Yeh HTTP request ke through hoti hai bilkul waisa jaise Postman se hum apni service ko call karte the, waisa hi ek service doosri service ko call karti hai. Iska proper tarika (RestTemplate ya Feign Client) hum aage  seekhenge.
