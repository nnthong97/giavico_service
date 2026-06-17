FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl rnd-document-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system giavico && adduser --system --ingroup giavico giavico
COPY --from=build /workspace/rnd-document-service/target/rnd-document-service-0.0.1-SNAPSHOT.jar app.jar
USER giavico
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
