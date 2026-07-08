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
