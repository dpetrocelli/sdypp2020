# Taller Docker 
En el marco de la asignatura de 41409 - Sistemas Distribuidos y Programación Paralela, se desarrolla un taller de capacitación en las tecnologías de contenedores (Docker) que se realizarán a lo largo de la jornada en dos fases:

* **Primer parte**: Un tutorial y ejercitación básica para aprender los aspectos más relevantes de la herramienta Docker (contenedores) y cómo desarrollar aplicaciones utilizando esta arquitectura.  A su vez, lograr conectar contenedores a través de una red Docker.

* **Segunda parte**: Un tutorial avanzado de Docker donde se integran sus capacidades para el desarrollo de aplicaciones distribuidas, buscando definir capacidades de balanceo de carga, redundancia y tolerancia a fallas. Para ello se trabaja en profundidad con la integración de volúmenes para persistir datos, balanceadores de carga para repartir carga, creación de clústeres de servicios, entre otros.

# Pre requisitos
## 1. Distribución Linux
Es necesario contar con una distribución linux actualizada (Ubuntu, Debian, CentOS, etc) con los paquetes de APT (Ubuntu, Debian) o YUM (CentOS) disponibles para trabajar con ellos.
## 2. Instalar Docker
Seguir las instrucciones de instalación en https://docs.docker.com/engine/install/
Se recomienda utilizar el script de instalación para Linux
```
curl -fsSL https://get.docker.com -o get-docker.sh
```
```
sh get-docker.sh
```
```
sudo usermod -aG docker $USER
```
Luego re loguear y verificar la instalación con el siguiente comando (puede tardar unos segundos)
```
docker run hello-world
```
## 3. Instalar Docker Compose
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
>Para avanzar con la primer parte, vamos a navegar hacia [docker-network-tutorial](https://github.com/dpetrocelli/sdypp2020/tree/master/TPS/No%20obligatorios/docker-network-tutorial)

>En ella, ingresar al directorio *tutorial* y leer el README.md para continuar con el tutorial. El directorio *terminado* contiene la configuración final, para consultar si surge algún error o simplemente correr el proyecto.

>Una vez finalizada la primer parte, avanzar con la segunda parte que se encuentra en "docker-network-tutorial-p2" y seguir los mismos pasos

