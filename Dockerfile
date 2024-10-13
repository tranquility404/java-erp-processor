# Use Maven for the build stage
FROM openjdk:17-jdk-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and other necessary files to download dependencies first
COPY pom.xml .

# Download the dependencies before copying the rest of the source files
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Second stage: use a lightweight OpenJDK image for running the app
FROM openjdk:17-jdk-alpine

# Set the working directory for the runtime environment
WORKDIR /app

# Copy the JAR file from the build stage to the runtime stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]