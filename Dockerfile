# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk AS compilacion
WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -g 1000 app && adduser -u 1000 -G app -S app

RUN mkdir -p /data/fotos-candidatos && chown -R app:app /data

COPY --from=compilacion /app/target/GestionPreElectoral-0.0.1-SNAPSHOT.jar app.jar
RUN chown app:app /app/app.jar

USER app

EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Bogota", "-jar", "/app/app.jar"]
