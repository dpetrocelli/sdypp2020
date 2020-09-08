# Taller Docker 
El objetivo es llevar a cabo un taller de capacitación en las tecnologías de contenedores (Docker) que se realizarán a lo largo de la jornada en tres fases:

* **Primer parte**: Presentación de conceptos básicos de Docker. Esencialmente veremos:
    -Conceptos básicos\
    -Docker vs Máquinas virtuales\
    -¿Cómo se compone?\
    -Aplicaciones Distribuidas\
    -Docker para desarrolladores\
    -Docker para operadores\
    -Arquitectura\
    -Popularidad y Adopción\
    -Bases prácticas\
    -Recomendaciones\

Acceso a Presentación: https://drive.google.com/file/d/1C1GO1pKXMNPXMNfG1f0G_6veMzLvFJGy/view?usp=sharing

 Un tutorial y ejercitación básica para aprender los aspectos más relevantes de la herramienta Docker (contenedores) y cómo desarrollar aplicaciones utilizando esta arquitectura.  A su vez, lograr conectar contenedores a través de una red Docker.

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
## 4.  Conocimientos previos
Es importante que antes de realizar el laboratorio de docker, los alumnos se internalicen en el uso del sistema operativo Linux y esencialmente el manejo de la consola del mismo, así como también aspectos de redes, comunicación y enfoques de programación distribuida.  Se recomienda tener claro los conceptos de TCP/IP, binding de puertos y repasar los comandos y servicios básicos para la administración de una plataforma linux.   
 
## Comenzar con el Taller Docker
Para comenzar con el taller, vamos a navegar hacia [docker-network-tutorial](https://github.com/dpetrocelli/sdypp2020/tree/master/TPS/No%20obligatorios/docker-network-tutorial)


