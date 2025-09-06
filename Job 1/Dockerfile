# Étape 1 : build avec Gradle + JDK 21
FROM gradle:8.10.0-jdk21 AS builder
WORKDIR /home/gradle/app
COPY . .
RUN gradle bootJar --no-daemon

# Étape 2 : exécuter avec JDK minimal (21)
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /home/gradle/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
