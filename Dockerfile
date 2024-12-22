FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/Lab3-1.1.jar /app/app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
EXPOSE 8080