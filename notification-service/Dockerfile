FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
RUN apk update && apk add --no-cache libc6-compat
COPY build/libs/notification-service-*.jar app.jar
RUN addgroup -S quadro && adduser -S quadro -G quadro
RUN mkdir -p /app/logs && chown -R quadro:quadro /app
USER quadro
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8085/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]