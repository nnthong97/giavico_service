FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl gateway-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system giavico && adduser --system --ingroup giavico giavico
COPY --from=build /workspace/gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar app.jar
USER giavico
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
