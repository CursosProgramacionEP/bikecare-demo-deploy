# ---- Etapa 1: compilar el .jar (el proyecto usa Java 24; JDK 25 compila con --release 24) ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -Dmaven.test.skip=true

# ---- Etapa 2: imagen liviana solo con el JRE ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
