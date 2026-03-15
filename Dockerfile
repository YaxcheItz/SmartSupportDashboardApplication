FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/smart-support-dashboard-0.0.1-SNAPSHOT.jar app.jar

# Usamos la variable de entorno PORT que nos da Render
ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT}", "app.jar"]
