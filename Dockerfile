FROM openjdk:8-jdk-alpine
COPY puke.jar /
COPY application.yml /
ENTRYPOINT ["java", "-jar", "puke.jar", "--spring.config.location=file:/"]
