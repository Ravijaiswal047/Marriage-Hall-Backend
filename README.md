# Marriage Hall — Microservices

A Spring Boot + Spring Cloud microservices project for booking marriage halls.

## Architecture

```
            ┌──────────────┐
  client ──►│ api-gateway  │  (8081)  single entry point + JWT check
            └──────┬───────┘
                   │ (routes via Eureka)
   ┌───────────────┼───────────────┬───────────────┐
   ▼               ▼               ▼               ▼
auth-service   hall-service   booking-service   review-service
  (9090)         (8087)          (8082)            (8086)
                                   │ Feign + circuit breaker
                                   ▼
                              hall-service

  discovery-server (8761) ── Eureka registry (all services register here)
  config-server    (8888) ── centralized config (serves jwt.secret etc.)
  mysql            (3307) ── shared database
```

## Services & Ports

| Service           | Port | Role                                              |
|-------------------|------|---------------------------------------------------|
| discovery-server  | 8761 | Eureka service registry                           |
| config-server     | 8888 | Centralized configuration                         |
| api-gateway       | 8081 | Single entry point, validates JWT, injects headers|
| auth-service      | 9090 | Signup / login, issues JWT                        |
| hall-service      | 8087 | CRUD halls (VENDOR role)                           |
| booking-service   | 8082 | Bookings + payments + vendor dashboard            |
| review-service    | 8086 | Hall reviews & ratings                            |

## Run everything (Docker)

```bash
docker-compose up --build
```

Startup order is handled automatically: mysql + discovery + config-server become
healthy first, then the rest start.

- Eureka dashboard: http://localhost:8761
- Config check:     http://localhost:8888/application/default
- API entry point:  http://localhost:8081

## Run a single service (IDE / Maven)

Open the **root pom.xml** as a project (imports all modules), or open a single
service's pom.xml. Then:

```bash
cd <service>
mvn spring-boot:run
```

Start order for local run: discovery-server → config-server → others.

## Build all

```bash
mvn clean install
```

## Tech

- Java 17, Spring Boot 3.5.0, Spring Cloud 2025.0.0
- Eureka (discovery), Spring Cloud Gateway, Spring Cloud Config
- OpenFeign + Resilience4j (inter-service calls + circuit breaker)
- Spring Security + JWT, Spring Data JPA + MySQL
- Docker + docker-compose, GitHub Actions CI/CD
```
