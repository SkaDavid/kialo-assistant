FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine

ENV JAVA_OPTS="-Xms64m -Xmx256m"

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]