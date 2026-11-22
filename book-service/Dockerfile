# ============================================================
# book-service
# Stage 1 : build Maven (JDK 21)
# Stage 2 : runtime JRE 21 + curl (indispensable au healthcheck Docker du TP)
# ============================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
