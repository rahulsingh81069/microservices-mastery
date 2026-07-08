Phase 2: Designing Your First Microservice

         Lesson 1: Ek Microservice "Design" Karna Matlab Kya?
                   Jab tum monolith app banate ho, tum soch nahi paate "yeh feature kis service mein jayega" sab kuch ek hi jagah hota hai. Microservices mein, sabse pehla aur sabse important kaam hai: decide karna ki tumhare system ko kitne services mein todna hai, aur kaunsa service kya responsibility uthayega.
                   Example  ek E-commerce app:
                    Agar yeh monolith hota, sab ek codebase mein hota:

                        User signup/login
                        Product listing
                        Cart
                        Order placing
                        Payment
                        Notification (email/SMS)
                    Microservices mein todne ka sahi tarika:

                        User Service
                        Product Service
                        Cart Service
                        order Service
                        Payment Service
                        Notification Service

                    ***Golden rule:
                       Service boundary business capability se decide hoti hai, technical convenience se nahi.

Hands-On Time (Phase 2, Part 1)
Scenario: Tumhe ek "Hospital Management System" banana hai jisme yeh features honge:
Patient registration
Doctor appointment booking
Prescription management
Billing/Payment
Lab test reports

        Phir yeh bhi likho: agar koi "Billing aur Lab-Reports" ko ek service mein combine karne ko bole, toh tum kya reasoning doge ki yeh sahi hai ya galat?

solution:
Service Responsibility
Patient registration Service Patient registration management
Doctor appointment booking Service Doctor availability, appointment management
Prescription management Service Prescription management
Billing/Payment Service Payment processing
Lab test reports Service Lab reports management

        Galat hoga combine karna, kyunki:

        Billing ka concern hai: payment gateway, invoices, refunds, insurance claims
        Lab Reports ka concern hai: test results, medical data, report generation
        Dono bilkul alag domain hain  ek "finance" hai, doosra "medical/clinical" hai
        Agar combine kiya, aur lab-report service ka load badha (bahut saare tests ho rahe hain), toh billing bhi slow ho jayega  jo galat hai, kyunki payment jaldi process hona chahiye

Har naye Codespace session mein:
1. mvn spring-boot:run
2. PORTS tab → globe icon click → "Continue" accept karo
3. Fresh URL copy karke Postman mein use karo
