   # Etapa 1: Compilar la aplicación con Maven
     FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
     WORKDIR /app
     COPY pom.xml .
     COPY src ./src
     # Compilamos saltando los tests para que sea más rápido
     RUN mvn clean package -DskipTests

     # Etapa 2: Crear la imagen final ligera
    FROM eclipse-temurin:21-jre-jammy
    WORKDIR /app
    # Copiamos el archivo .jar generado en la etapa anterior
    COPY --from=build /app/target/smart-support-dashboard-0.0.1-SNAPSHOT.jar
      app.jar

    # Exponemos el puerto que usa Spring Boot
    EXPOSE 8080

    # Comando para ejecutar la aplicación
    ENTRYPOINT ["java", "-jar", "app.jar"]