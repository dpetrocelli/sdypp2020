FROM openjdk:latest
COPY ./src /usr/src/app
WORKDIR /usr/src/app
RUN javac Cliente.java
CMD ["java", "Cliente", "server", "4444"]
