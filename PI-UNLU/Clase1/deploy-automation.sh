#!/bin/bash
#[STEP 0] - crear ejecutable JAR
mvn clean;
mvn package;
#[STEP 1] - construir imagen (en base al Dockerfile)
docker build -t dpetrocelli/pruebaclase2:latest .
#[STEP 2] - subir a dockerHUB la imagen creada
docker push dpetrocelli/pruebaclase2:latest
#[STEP 3] - parar y eliminar la versión anterior del contenedor
docker stop probandoencasa
docker rm probandoencasa
#[STEP 4] - poner a correr la nueva versión del contenedor
docker run --name probandoencasa -d -p 9090:9090 dpetrocelli/pruebaclase2:latest
