FROM openjdk:latest
COPY ./src /usr/src/app
WORKDIR /usr/src/app
RUN javac Servidor.java
EXPOSE 4444
CMD ["java", "Servidor", "4444"]
