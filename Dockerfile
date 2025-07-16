FROM maven:3.9.6-eclipse-temurin-21 AS build

COPY . /app

WORKDIR /app

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=build /app/target/undertow-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]