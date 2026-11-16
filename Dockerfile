# Test-runner image: builds the framework and runs the suite inside the container.
# Build:  docker build -t api-tests .
# Run:    docker run --rm -e ENV=qa api-tests
# Extra:  docker run --rm api-tests -Psmoke

FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

# Cache dependencies in their own layer so source edits do not re-download the world.
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline

COPY src ./src
COPY testng.xml testng-smoke.xml ./

ENV ENV=qa

ENTRYPOINT ["mvn", "-B", "-ntp", "test"]
