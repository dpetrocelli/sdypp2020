# Taller Docker 
En el marco de la asignatura de 41409 - Sistemas Distribuidos y Programación Paralela, se desarrolla un proyecto de capacitación en las tecnologías de contenedores (Docker) que se realizarán a lo largo de la jornada en dos fases:

* **Primer parte**: Un tutorial y ejercitación básica para aprender los aspectos más relevantes de la herramienta Docker (contenedores) y cómo desarrollar aplicaciones utilizando esta arquitectura.  A su vez, lograr conectar contenedores a través de una red Docker.

* **Segunda parte**: Un tutorial avanzado de Docker donde se integran sus capacidades para el desarrollo de aplicaciones distribuidas, buscando definir capacidades de balanceo de carga, redundancia y tolerancia a fallas. Para ello se trabaja en profundidad con la integración de volúmenes para persistir datos, balanceadores de carga para repartir carga, creación de clústeres de servicios, entre otros.

# 
Pre requisitos
## Instalar Docker
Seguir las instrucciones de instalación en https://docs.docker.com/engine/install/
Se recomienda utilizar el script de instalación para Linux
```
curl -fsSL https://get.docker.com -o get-docker.sh
```
```
sh get-docker.sh
```
```
sudo usermod +aG docker $USER
```
Luego re loguear y verificar la instalación con el siguiente comando (puede tardar unos segundos)
```
docker run hello-world
```
## Descargar la imagen para desarrollar en java
En este tutorial se utiliza la última versión de la imagen *openjdk* (v14) disponible en Docker hub (https://hub.docker.com/_/openjdk)
```
docker pull openjdk:latest
```
## Instalar Docker Compose
Seguir las instrucciones de instalación en https://docs.docker.com/compose/install/
```
sudo curl -L "https://github.com/docker/compose/releases/download/1.25.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```
```
sudo chmod +x /usr/local/bin/docker-compose
```
Probar la instalación
```
docker-compose --version
```
## Continuar el tutorial
>Para avanzar con la primer parte, ingresar a la carpeta "docker-network-tutorial".

>En ella, ingresar al directorio *tutorial* y leer el README.md para continuar con el tutorial. El directorio *terminado* contiene la configuración final, para consultar si surge algún error o simplemente correr el proyecto.

>Una vez finalizada la primer parte, avanzar con la segunda parte que se encuentra en "docker-network-tutorial-p2" y seguir los mismos pasos

