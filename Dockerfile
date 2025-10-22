FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /opt/mivoto
COPY --from=builder /app/target/mivoto-backend-*.jar app.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-Xms256m","-Xmx512m","-jar","app.jar"]
