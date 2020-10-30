#!/bin/bash
#[STEP 0] - crear ejecutable JAR
mvn clean;
mvn package;
#[STEP 1] - bajar si habia una versión anterior
docker-compose -f docker-compose.yaml down
#[STEP 2] - forzar la creación de la imagen y levantar los servicios
docker-compose -f docker-compose.yaml up --build 
