FROM gradle:8.5-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle ./gateway ./gateway
RUN chmod +x ./gateway/gradlew
RUN cd ./gateway && ./gradlew clean bootJar -x test
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/gateway/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]