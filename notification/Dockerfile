FROM gradle:8.5-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle ./notification ./notification
RUN chmod +x ./notification/gradlew
RUN cd ./notification && ./gradlew clean bootJar -x test
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/notification/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]