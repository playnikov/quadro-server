FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY build/libs/auth-service-*.jar app.jar
RUN addgroup -S quadro && adduser -S quadro -G quadro
RUN mkdir -p /app/logs && chown -R quadro:quadro /app
USER quadro
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8081/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]