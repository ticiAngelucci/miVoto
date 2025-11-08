# Etapa de build (compila el proyecto con Maven)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa runtime (solo corre el JAR)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
# El puerto 8080 es el puerto interno del contenedor.
EXPOSE 8080

# ENTRYPOINT que asegura que la app lea el puerto de la variable de entorno PORT 
# proporcionada por Render, y lo asigne a la propiedad 'server.port' de Spring Boot.
ENTRYPOINT ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
