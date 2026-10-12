# ─── Etapa 1: compilar con Maven ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Se copia primero el pom.xml para que Docker cachee las dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Luego el codigo fuente (las pruebas ya corren en el pipeline de CI)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ─── Etapa 2: imagen final ligera (solo JRE, sin Maven ni codigo fuente) ──────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios: el proceso no corre como root
RUN addgroup -S brasa && adduser -S brasa -G brasa

COPY --from=builder /app/target/*.jar app.jar
RUN chown brasa:brasa app.jar
USER brasa

# Valores por defecto; docker-compose y Azure los sobreescriben
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8080

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]