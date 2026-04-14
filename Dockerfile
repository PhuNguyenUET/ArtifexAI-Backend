FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

COPY mail_template ./mail_template
COPY prompt_template ./prompt_template
COPY style_template ./style_template

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN mkdir -p /app/credentials

COPY --from=build /app/target/*.jar app.jar

COPY --from=build /app/mail_template ./mail_template
COPY --from=build /app/prompt_template ./prompt_template
COPY --from=build /app/style_template ./style_template

ENV GOOGLE_APPLICATION_CREDENTIALS=/app/credentials/google-credentials.json

EXPOSE 7070

ENTRYPOINT ["java", "-jar", "app.jar"]

