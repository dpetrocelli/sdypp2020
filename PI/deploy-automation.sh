#!/bin/bash
mvn clean;
mvn package;
# --> ejecutable fue a parar a target/
cd target/;
java -jar ex1-0.0.1-SNAPSHOT.jar;