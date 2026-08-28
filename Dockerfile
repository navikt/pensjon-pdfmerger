FROM maven:3-eclipse-temurin-25 AS build

# Cache maven dependencies
WORKDIR /build
COPY pom.xml /build/pom.xml
RUN mvn dependency:go-offline

# Build application
COPY .editorconfig /build/
COPY src /build/src
RUN find .
RUN mvn -Dkotlin.format.skip=true verify

FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25

WORKDIR /app
COPY --from=build /build/target/pdfmerger-jar-with-dependencies.jar /app/pdfmerger.jar

USER 65532:65532
CMD [ "-jar", "/app/pdfmerger.jar"]
