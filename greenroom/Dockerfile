FROM gradle:8.5-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle ./greenroom ./greenroom
RUN chmod +x ./greenroom/gradlew
RUN cd ./greenroom && ./gradlew clean bootJar -x test
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/greenroom/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]