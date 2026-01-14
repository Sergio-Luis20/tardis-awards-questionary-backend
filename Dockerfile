FROM openjdk:21-jdk-slim AS build
COPY . .

RUN --mount=type=secret,id=private_base64,dst=/etc/secrets/private_base64 \
    base64 -d private_base64 > /src/main/resources/private.key
RUN --mount=type=secret,id=public_base64,dst=/etc/secrets/public_base64 \
    base64 -d public_base64 > /src/main/resources/public.key
RUN --mount=type=secret,id=questions_base64,dst=/etc/secrets/questions_base64 \
    base64 -d questions_base64 > /src/main/resources/questions.csv

RUN chmod +x ./mvnw
RUN ./mvnw clean package -q -DskipTests

FROM openjdk:21-jdk-slim

EXPOSE 8080
COPY --from=build /target/tardis-awards-questionary.jar tardis-awards-questionary.jar
ENTRYPOINT ["java", "-jar", "tardis-awards-questionary.jar", "--spring.profiles.active=prod"]