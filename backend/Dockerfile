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
# Render mapeará el tráfico de su puerto público a este puerto.
EXPOSE 8080

# ENTRYPOINT que asegura que la app lea el puerto de la variable de entorno PORT 
# proporcionada por Render, y lo asigne a la propiedad 'server.port' de Spring Boot.
# Usamos un valor por defecto (8080) por si se corre localmente.
ENTRYPOINT ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
```
**Cambio Clave:** Hemos reemplazado `java $JAVA_OPTS -jar` por `java -Dserver.port=$PORT -jar`.

### 2. Verificación en el Backend de Java

Si tu proyecto usa **Spring Boot**, la propiedad `-Dserver.port=$PORT` en el `ENTRYPOINT` debería ser suficiente, ya que el *framework* la leerá automáticamente.

**Si no usas Spring Boot**, revisa el código de tu servidor Java para asegurar que, al iniciar, **lea la variable de entorno `PORT`** y levante el servidor en ese puerto:

```java
// Ejemplo conceptual en Java/Spring Boot (no necesita ser modificado si usas Spring)
String port = System.getenv("PORT");
if (port != null) {
    // Configura el servidor para usar ese 'port'
}
