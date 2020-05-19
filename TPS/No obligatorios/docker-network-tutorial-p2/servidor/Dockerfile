FROM openjdk:latest
COPY ./src /usr/src/app
WORKDIR /usr/src/app
RUN yum update -y 
RUN yum install iputils -y
RUN javac Servidor.java
EXPOSE 4444
CMD ["java", "Servidor"]