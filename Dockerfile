# Build stage using Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
# Ensure the static folder (with our frontend) is included in the build
RUN mvn clean package -DskipTests

# Run stage using lightweight Java 17
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose the internal port
EXPOSE 8080

# Run the JAR with a dynamic port assignment for Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]

