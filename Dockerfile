FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src/ src/
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache su-exec wget

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN mkdir -p /portifolium-files /var/lib/portifolium/files && \
    chown -R appuser:appgroup /portifolium-files /var/lib/portifolium /app && \
    chmod +x /usr/local/bin/docker-entrypoint.sh

USER root

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
