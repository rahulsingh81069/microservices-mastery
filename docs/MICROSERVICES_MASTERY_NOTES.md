# Microservices Mastery — Complete Revision Notes

> A learning journal covering Phase 1 through Phase 5 of building a microservices-based system from scratch, using Java + Spring Boot. Written for revision and for anyone else following the same path — includes concepts, real errors faced, and how they were solved.

**Tech Stack:** Java 21, Spring Boot, Spring Cloud (Eureka, Gateway, OpenFeign, Resilience4j), H2 (in-memory DB), Maven, GitHub Codespaces (cloud dev environment), Postman.

---

## Table of Contents

1. Phase 1: Foundations
2. Phase 2: Your First Microservice
3. Phase 3: Service-to-Service Communication
4. Phase 4: Service Discovery & API Gateway
5. Phase 5: Resilience
6. Master List — Problems Faced & Fixes
7. Key Takeaways for Any Architect

---

## Phase 1: Foundations

### Core Concept: Monolith vs Microservices

A monolith is one codebase, one deployable unit, one database. It's simple for small teams but becomes a liability at scale:

- One bug can crash the entire application (no fault isolation)
- You can't scale just the busy part — you must scale everything together
- Deployments become slow and risky as the team grows
- You're locked into one tech stack for the whole app

A microservice architecture splits the system into small, independent services — each with its own codebase, own database, and own deployment pipeline, communicating over the network (REST, messaging, etc.).

### Golden Rule

Microservices are a **trade-off**: you gain scalability, fault isolation, and independent deployability, but you pay with network overhead, operational complexity, and distributed-system problems. A good architect picks monolith-vs-microservices based on actual need — not trend. Companies like Amazon and Netflix started as monoliths and migrated only once scale demanded it.

### Bounded Context (Domain-Driven Design)

Before writing any code, decide how many services you need and what each one owns. Rule: **one service = one business capability.**

Example (E-commerce): User Service, Product Service, Cart Service, Order Service, Payment Service, Notification Service — each independent, each owning only its own data.

**Two anti-patterns to avoid:**

- **Distributed Monolith** — too few, overly broad services (e.g., merging Restaurant + Delivery into one service just because they're "related"). You get microservices overhead (network calls, separate DBs) _and_ monolith-style tight coupling — worst of both worlds.
- **Nanoservices** — too many, overly granular services (e.g., a separate service just for "AddressValidation"). Excessive network calls and operational overhead.

---

## Phase 2: Your First Microservice

### Layered Architecture Built

### Entity Layer

`@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` — marks a class as a DB table, defines its primary key. Lombok's `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` auto-generate getters/setters/constructors, removing boilerplate.

**Why `@Id` is mandatory:** without it, Spring Boot fails at startup — JPA requires every entity to have a uniquely identifiable primary key. This is Spring's "fail-fast" principle.

### Repository Layer

```java
public interface UserRepository extends JpaRepository<User, Long> { }
```

No implementation needed — Spring Data JPA generates one at runtime, providing `save()`, `findById()`, `findAll()`, `deleteById()`, etc.

### Service Layer

This is where business rules live (e.g., "email must be unique before creating a user"), not in the Repository or Controller.

`@Autowired` = Dependency Injection — Spring provides the object for you.

### Controller Layer

`@RestController` + `@RequestMapping("/api/users")` exposes endpoints to the outside world. `@RequestBody` parses JSON into an object; `@PathVariable` reads URL path values; `@RequestParam` reads query parameters.

### Global Exception Handling

Without it, every unhandled exception becomes a generic `500 Internal Server Error`. Fix: custom exception classes + `@RestControllerAdvice` + `@ExceptionHandler`, mapping to meaningful HTTP status codes (404, 409, 400).

**Key clarification:** Returning a message via `ResponseEntity` is not the same as logging. Loggers write only to the server console; `ResponseEntity` sends JSON directly to the client.

### Tooling / Environment Learnings

- Java version vs Maven's JAVA_HOME can differ if multiple JDKs are installed.
- Production systems standardize on LTS Java releases (17, 21, 25...).
- VS Code + Java/Spring Boot extension packs works well as a lightweight, multi-purpose editor.

---

## Phase 3: Service-to-Service Communication

### Why Split Into Real Services Now

User and Product are different bounded contexts, so they were split into genuinely separate services (`user-service`, `product-service`, `order-service`), each with its own port and own in-memory database.

### Ports

Two processes cannot bind to the same port on one machine. Every service got an explicit `server.port` (8081, 8082, 8083).

### Feign Client — service-to-service HTTP calls made simple

```java
@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
```

Calling `userClient.getUserById(id)` looks like a normal method call but fires a real HTTP GET request under the hood — called **"location transparency."**

### DTO Pattern (Data Transfer Object)

`order-service` does not reuse `user-service`'s actual `User` entity — it defines its own lightweight `UserDTO`.

**Why:** Using the real Entity would tightly couple Order Service to User Service's internal implementation. A DTO keeps services loosely coupled and independently evolvable.

### Order Entity Only Stores IDs, Not Full Objects

`Order` stores `userId` and `productId`, not full data, because:

- Duplicating data elsewhere causes it to go stale.
- **Known unresolved problem:** if a User/Product is deleted, references in Order Service become "orphaned." Real solutions (soft delete, event-driven updates) are deferred to **Phase 8 (Event-Driven Architecture)**.

### Handling Downstream Failures (intro to resilience)

When `user-service` was stopped, an order attempt gave a generic `500`. Fixed by catching `FeignException`, translating it into `ServiceUnavailableException`, mapped to HTTP `503` in the Global Exception Handler.

---

## Phase 4: Service Discovery & API Gateway

### The Problem With Hardcoded URLs

`@FeignClient(url = "http://localhost:8081")` doesn't scale — addresses change, multiple instances can exist, manually tracking availability is unmanageable.

### Eureka Server (Service Registry)

Analogy: a directory service — every service registers itself on startup, and others ask Eureka "where is user-service right now?" instead of hardcoding an address.

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication { ... }
```

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Each client service adds `@EnableDiscoveryClient` + Eureka Client dependency + `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`.

Feign Clients then drop the hardcoded URL: `@FeignClient(name = "user-service")`.

### API Gateway (Single Entry Point)

Analogy: one main reception desk instead of visitors needing to know every department's floor.

```properties
server.port=8080
spring.cloud.gateway.server.webmvc.routes[0].id=user-service
spring.cloud.gateway.server.webmvc.routes[0].uri=lb://user-service
spring.cloud.gateway.server.webmvc.routes[0].predicates[0]=Path=/user-service/**
spring.cloud.gateway.server.webmvc.routes[0].filters[0]=StripPrefix=1
```

- `lb://` = load-balanced, resolved through Eureka.
- `StripPrefix=1` removes the first path segment before forwarding.

**Gotcha:** `spring-cloud-starter-gateway-server-webmvc` (newer Servlet-based variant) uses a different property prefix than the older WebFlux variant. Confirmed via official docs: `spring.cloud.gateway.server.webmvc.routes` is correct (not `spring.cloud.gateway.routes` or the deprecated `spring.cloud.gateway.mvc.routes`).

---

## Phase 5: Resilience

### Circuit Breaker Pattern

Analogy: a household MCB — if something keeps failing, it trips and stops the current entirely.

**Three states:**

- **CLOSED** — normal, requests flow through
- **OPEN** — failure threshold exceeded, calls aren't attempted, fallback returned immediately (fail-fast)
- **HALF-OPEN** — after a wait, limited test calls check if the dependency recovered

**Why it matters:** without it, repeated calls to a dead dependency can cause **cascading failure**.

### Configuration

```properties
resilience4j.circuitbreaker.instances.userServiceCB.sliding-window-size=5
resilience4j.circuitbreaker.instances.userServiceCB.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.userServiceCB.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.userServiceCB.permitted-number-of-calls-in-half-open-state=3
```

### Fallback Methods Must Distinguish Error Types

Mistake: any failure (even legitimate "404 - not found") was reported as "Service unavailable."

**Fix:**

```java
public UserDTO getUserFallback(Long userId, Throwable throwable) {
    if (throwable instanceof FeignException.NotFound) {
        throw new ResourceNotFoundException("User not found with id: " + userId);
    }
    throw new ServiceUnavailableException("User Service is currently unavailable. Please try again later.");
}
```

**Lesson:** Circuit breakers should trip on genuine infrastructure failures, not ordinary business-logic responses.

### The Self-Invocation Bug (the big one this phase)

**Symptom:** `@CircuitBreaker` annotated methods did nothing — raw exceptions leaked through as unhandled 500s.

**Root cause (confirmed via stack trace):** Spring annotations work through AOP proxies. Calling an annotated method from _within the same class_ bypasses the proxy entirely — a well-known Spring gotcha called **self-invocation**.

**Fix:** move the annotated method into its own separate `@Component` class, injected into `OrderService`, so the call happens _between two different beans_ and correctly passes through the proxy.

```java
@Component
public class UserServiceClient {
    @Autowired private UserClient userClient;

    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "getUserFallback")
    public UserDTO getUserById(Long userId) { return userClient.getUserById(userId); }

    public UserDTO getUserFallback(Long userId, Throwable throwable) { ... }
}
```

**Note:** wrong service-startup order was briefly suspected as the cause but was a separate, unrelated issue — confirmed by testing each fix in isolation rather than assuming.

### Startup Order Matters

Recommended order: **Eureka Server → User/Product Service → Order Service → API Gateway.**

---

## Master List — Problems Faced & Fixes

| #   | Problem                                                             | Root Cause                                                                                   | Fix                                                                        |
| --- | ------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| 1   | Maven `Non-resolvable parent POM` / SSL `PKIX path building failed` | Network SSL inspection interfering with Java's certificate trust store                       | Used GitHub Codespaces to bypass local network for builds/runs             |
| 2   | `mvnw.cmd` not recognized in PowerShell                             | PowerShell doesn't run local scripts by name without `.\` prefix                             | Used `.\mvnw.cmd` or globally installed `mvn`                              |
| 3   | Codespace port shows `401 Unauthorized` from Postman                | Codespaces ports default to Private visibility                                               | Set port visibility to Public in PORTS tab                                 |
| 4   | Codespace URL gives `404` for everything                            | One-time-per-session "access dev port" interstitial must be accepted via browser click first | Click port's globe icon, press "Continue" before using Postman             |
| 5   | Codespace URL/subdomain changes after restart                       | Codespaces can issue a new forwarding subdomain                                              | Always copy fresh "Forwarded Address" from PORTS tab                       |
| 6   | `git push` rejected — "fetch first"                                 | Local and Codespace both had commits the other didn't                                        | `git pull` (resolve merge, `Esc` then `:wq` in Vim), then `git push`       |
| 7   | Maven "cannot find symbol: class X"                                 | Missing `import` statement                                                                   | Add correct import                                                         |
| 8   | Spring Cloud dependency "version is missing"                        | Missing `<dependencyManagement>` BOM import                                                  | Add `<properties>` + `<dependencyManagement>` block together               |
| 9   | API Gateway routes never matched (404)                              | Wrong config property prefix for `webmvc` Gateway variant                                    | Confirmed via official docs: `spring.cloud.gateway.server.webmvc.routes[]` |
| 10  | Circuit Breaker fallback fired even for normal 404                  | Fallback treated every exception as "unavailable"                                            | Check `throwable instanceof FeignException.NotFound` explicitly            |
| 11  | Circuit Breaker/fallback did nothing, raw 500s leaked               | Self-invocation — bypassed Spring's AOP proxy                                                | Move annotated method to its own `@Component` class                        |

---

## Key Takeaways for Any Architect

1. Split services by business capability, not convenience.
2. Every service owns its own database.
3. Reference by ID, not full object — and know this creates a data-consistency problem to solve later (soft deletes, events).
4. DTOs, not shared Entities, cross service boundaries.
5. Every unhandled exception is a design gap — always map to a meaningful HTTP status.
6. Service discovery removes hardcoded addresses, enabling safe scaling/redeployment.
7. An API Gateway is the one address a client needs to know.
8. Downstream dependencies will fail — plan with circuit breakers and fallbacks, distinguishing "broken" from "correctly reported error."
9. Framework behavior changes between versions — check official docs when confused.
10. Read the stack trace before guessing.

---

_Document generated as a running study log — Phases 1 through 5, Microservices Mastery project._
