FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /opt/mivoto
COPY --from=builder /app/target/mivoto-backend-0.1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Xms256m","-Xmx512m","-jar","app.jar"]
