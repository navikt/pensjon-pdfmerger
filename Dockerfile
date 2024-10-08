FROM maven:3-eclipse-temurin-21 AS build

# Cache maven dependencies
WORKDIR /build
COPY pom.xml /build/pom.xml
RUN mvn dependency:go-offline

# Build application
COPY .editorconfig /build/
COPY src /build/src
RUN find .
RUN mvn -Dkotlin.format.skip=true verify

FROM eclipse-temurin:21

RUN apt-get update && apt-get install -y dumb-init && rm -rf /var/lib/apt/lists/*

RUN mkdir /app
COPY --from=build /build/target/pdfmerger-jar-with-dependencies.jar /app/pdfmerger.jar

CMD [ "/usr/bin/dumb-init", "java", "-jar", "/app/pdfmerger.jar"]
