# Phase 7: Observability

## Goal

- Centralized Logging - dekhna kaha kya ho raha hai, sab services ke logs ek jagah
- Distributed Tracing - ek request jab multiple services se hokar guzre, uska poora path track karna
- Metrics - system health monitor karna (Prometheus + Grafana)

## Why this matters

Abhi tak humne har service ka log alag terminal mein dekha. Real production
mein 50+ services honge, alag alag machines pe chal rahi hongi - terminal
dekh ke debug karna impossible hai. Observability tools ek central jagah
se sab dikhate hain.

## Services in this phase

- config-server, eureka-server, api-gateway, user-service,
  product-service, order-service (Phase 6 se copy, logging/tracing add karenge)
- Zipkin (distributed tracing)
- Prometheus + Grafana (metrics) - agar time allow kare
