FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/api-gateway-*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]