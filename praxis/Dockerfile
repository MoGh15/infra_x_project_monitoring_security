# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# 8080 = API, 8081 = actuator (health + prometheus metrics)
EXPOSE 8080 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
