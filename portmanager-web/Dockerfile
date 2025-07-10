FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/target/portmanager-web-*.jar app.jar
ENV BACKEND_URL=http://backend:8080/api
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
