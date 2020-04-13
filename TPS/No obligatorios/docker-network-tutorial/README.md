Este proyecto contiene un tutorial cuyo objetivo es aprender los aspectos básicos para desarrollar utilizando Docker y ser capaz de conectar contenedores a través de una red.
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
Ingresar al directorio *tutorial* y leer el README.md para continuar con el tutorial. El directorio *terminado* contiene la configuración final, para consultar si surge algún error o simplemente correr el proyecto.