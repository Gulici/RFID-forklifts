FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder app/target/*.jar app.jar

# ENV JAVA_OPTS="-Dspring.config.location=file:/config/"
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

ENTRYPOINT ["java", "-Dspring.config.location=file:/config/", "-jar", "app.jar"]