FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/*.jar orderservice.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "orderservice.jar"]