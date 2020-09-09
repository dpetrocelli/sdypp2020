# Taller Docker 
Docker es un proyecto de código abierto que permite desplegar aplicaciones dentro de contenedores de software de forma automatizada. Un contenedor es un proceso o conjunto de procesos que se ejecutan de forma aislada al resto del sistema, lo que permite que el contenedor tenga sus propias versiones de aplicaciones y librerias. Esto supone grandes ventajas tanto a desarrolladores, testers y administradores de sistemas, que solo tienen que preocuparse de tener un contenedor configurado y ejecutarlo.

En este taller se explicarán los conceptos básicos de Docker y se trabajará con actividades prácticas de laboratorio guiadas para implementar y trabajar con contenedores.
Para ello, trabaj
que se realizarán a lo largo de la jornada en tres fases:

* **Primer parte**: Presentación "teórica" de conceptos básicos de Docker. En este apartado veremos:
    -  Conceptos básicos
    -  Docker vs Máquinas virtuales
    -  ¿Cómo se compone?
    -  Aplicaciones Distribuidas
    -  Docker para desarrolladores
    -  Docker para operadores
    -  Arquitectura
    -  Popularidad y Adopción
    -  Bases prácticas
    -  Recomendaciones

 <a href="https://drive.google.com/file/d/1C1GO1pKXMNPXMNfG1f0G_6veMzLvFJGy/view?usp=sharing" target="blank">Acceso a Presentación - Introducción a Docker</a>

* **Segunda parte**: Un tutorial y ejercitación básica para aprender los aspectos más relevantes de la herramienta Docker (contenedores) y cómo desarrollar aplicaciones utilizando esta arquitectura. A su vez, lograr conectar contenedores a través de una red Docker.

* **Tercera parte (*)** : Un tutorial avanzado de Docker donde se integran sus capacidades para el desarrollo de aplicaciones distribuidas, buscando definir capacidades de balanceo de carga, redundancia y tolerancia a fallas. Para ello se trabaja en profundidad con la integración de volúmenes para persistir datos, balanceadores de carga para repartir carga, creación de clústeres de servicios, entre otros.
(*) Esta parte del taller no se realizará en esta clase, podrán realizarlo con el paso a paso que está detallado en el tutorial.

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
```bash
docker run hello-world
...
Unable to find image 'hello-world:latest' locally
latest: Pulling from library/hello-world
0e03bdcc26d7: Pull complete 
Digest: sha256:7f0a9f93b4aa3022c3a4c147a449bf11e0941a1fd0bf4a8e6c9408b2600777c5
Status: Downloaded newer image for hello-world:latest

Hello from Docker!
This message shows that your installation appears to be working correctly.
....
```
## 3. Instalar Docker Compose
Seguir las instrucciones de instalación en https://docs.docker.com/compose/install/
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/1.25.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```
```bash
sudo chmod +x /usr/local/bin/docker-compose
```
Probar la instalación
```bash
docker-compose --version
....
docker-compose version 1.25.5, build 8a1c60f6
....
```
## 4.  Conocimientos previos
Es importante que antes de realizar el laboratorio de docker, los alumnos se internalicen en el uso del sistema operativo Linux y esencialmente el manejo de la consola del mismo, así como también aspectos de redes, comunicación y enfoques de programación distribuida.  Se recomienda tener claro los conceptos de TCP/IP, binding de puertos y repasar los comandos y servicios básicos para la administración de una plataforma linux.   
 
## Comenzar con el Taller Docker
Para comenzar con el taller, vamos a navegar hacia [docker-network-tutorial](https://github.com/dpetrocelli/sdypp2020/tree/master/TPS/No%20obligatorios/docker-network-tutorial)


