FROM maven:3.9.6-eclipse-temurin-21 AS build

COPY . /app
WORKDIR /app

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

COPY wildfly-slim /opt/wildfly

COPY --from=build /app/target/*.war /opt/wildfly/standalone/deployments/app.war

EXPOSE 8080

CMD ["/opt/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]