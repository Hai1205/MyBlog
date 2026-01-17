# multi-stage Dockerfile for Spring Boot (Maven + OpenJDK 21)
# Place this Dockerfile in each service folder (discovery, gateway, auth, blog, stats)

# -------- build stage --------
FROM maven:3.9.4-eclipse-temurin-21 as build
WORKDIR /workspace

# copy full repository into build context so parent POM and modules are available
COPY . .

# build jar for this module and its reactor dependencies (skip tests)
RUN mvn -f backend/pom.xml -B -DskipTests package

# find the built jar (module target)
RUN ls -lah backend/stats-service/target || true
RUN JAR_FILE=$(ls backend/stats-service/target/*.jar | head -n 1) && echo "Jar: $JAR_FILE" && cp $JAR_FILE /workspace/app.jar

# -------- runtime stage --------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Add a non-root user for safety (optional)
RUN useradd -m appuser
USER appuser

COPY --from=build /workspace/app.jar ./app.jar

# Expose default port (the actual binding comes from Spring config)
EXPOSE 8087

# JVM options can be adjusted via env JAVA_OPTS
ENV JAVA_OPTS="-Xss512k -Xms256m -Xmx512m"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar" ]