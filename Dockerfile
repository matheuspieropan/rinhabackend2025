FROM maven:3.9.6-eclipse-temurin-21 AS build

COPY . /app

WORKDIR /app

RUN mvn clean package -DskipTests

FROM quay.io/wildfly/wildfly:36.0.0.Final-jdk21

COPY --from=build /app/target/*.war /opt/jboss/wildfly/standalone/deployments/app.war

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]