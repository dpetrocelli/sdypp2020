#!/bin/bash 
# Removing everything 
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
docker volume rm $(docker volume ls -q)

docker-compose up -d initialnode
sleep 30
docker-compose scale server-joiner=1
sleep 7
docker-compose scale server-joiner=2
sleep 7
docker-compose scale agent=3

