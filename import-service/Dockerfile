FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/import-service-1.0-SNAPSHOT.jar /app/app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]