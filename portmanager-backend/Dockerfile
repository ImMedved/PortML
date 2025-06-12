FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM openjdk:17-slim
RUN apt-get update && apt-get install -y curl
WORKDIR /app

COPY --from=build /build/target/backend.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
