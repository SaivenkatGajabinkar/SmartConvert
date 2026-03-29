# Build stage using Maven and Java 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Ensure the static folder (with our frontend) is included in the build
RUN mvn clean package -DskipTests

# Run stage using lightweight OpenJDK 17
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose the internal port
EXPOSE 8080

# Run the JAR with a dynamic port assignment for Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
