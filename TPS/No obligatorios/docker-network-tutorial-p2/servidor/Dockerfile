FROM bitnami/minideb:latest
COPY ./src /usr/src/app
WORKDIR /usr/src/app
RUN apt update -y
RUN apt install openjdk-11-jdk -y
RUN javac Servidor.java
EXPOSE 4444
CMD ["java", "Servidor"]