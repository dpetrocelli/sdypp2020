FROM adoptopenjdk/openjdk11:alpine-slim
COPY server.jar /usr/src/server.jar
WORKDIR /usr/src/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/src/server.jar"]