FROM openjdk:17-jdk-slim
# Working dir INSIDE the dock container
WORKDIR /backend-app
# Copying Inside container
COPY target/java-erp-scrapper.jar /backend-app/java-erp-scrapper.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/backend-app/java-erp-scrapper.jar"]