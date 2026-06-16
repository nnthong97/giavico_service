FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system giavico && adduser --system --ingroup giavico giavico
COPY --from=build /workspace/target/beverage-formulation-engine-0.0.1-SNAPSHOT.jar app.jar
USER giavico
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
