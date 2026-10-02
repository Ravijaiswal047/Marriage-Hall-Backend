# ============================================================
#  Multi-stage Dockerfile  (har Spring Boot service ke liye same)
# ============================================================

# ---------- Stage 1: BUILD ----------
# Bhaari image (Maven + full JDK) — sirf jar banane ke liye
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# pehle sirf pom.xml copy -> dependency layer cache hoti hai.
# Agar sirf code badla, deps dobara download nahi honge (fast rebuild).
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# ab source copy karke package banao
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# ---------- Stage 2: RUNTIME ----------
# Patli image (sirf JRE) -> final image chhoti + secure
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Stage-1 se sirf jar uthao (Maven/source final image mein nahi jaata)
COPY --from=build /app/target/*.jar app.jar

# documentation-only (actual publish compose ke "ports" se hota hai)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
