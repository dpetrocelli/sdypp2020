FROM openjdk:latest
COPY target/ex1-0.0.1-SNAPSHOT.jar /usr/src/ex1-0.0.1-SNAPSHOT.jar
WORKDIR /usr/src/
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/usr/src/ex1-0.0.1-SNAPSHOT.jar"]