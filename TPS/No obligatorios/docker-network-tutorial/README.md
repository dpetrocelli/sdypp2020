Bienvenidos al tutorial Docker network - Parte 1. 
Este tutorial te guiará en:
- Comprender los aspectos básicos de Docker
- Utilizar y crear tus propias imágenes
- Configurar contenedores utilizando Dockerfiles
- Crear volúmenes para persistir y actualizar cambios
- Crear una red con Docker compose, conectando dos programas en contenedores distintos

# Sección 1 -- Conceptos de Docker
En Docker existen tres conceptos básicos que hay que entender para utilizar la tecnología:
- Imagen: paquete que contiene todos los recursos necesarios para correr una aplicación. Las imágenes son distribuibles y autocontenidas; funcionan igual en cualquier entorno de host gracias a la virtualización de Docker. En este tutorial se crearán dos imágenes (Servidor y Cliente), cada una correspondiente a una aplicación.
- Contenedor: espacio donde corre nuestra aplicación, correspondiente a una instancia de la imagen generada. Los contenedores pueden iniciarse y detenerse a medida. Varios contenedores pueden partir de la misma imagen.
- Volumen: directorio lógico que utilizan los contenedores para obtener o almacenar información por fuera del contenedor.

# Sección 2 -- Obtener el entorno
El primer requisito para crear un contenedor es buscar un entorno que pueda correr la aplicación a desarrollar. Afortunadamente, la comunidad de Docker tiene un repositorio de imágenes actualizado con muchos recursos ya empaquetados y listos para usar. En este caso, utilizaremos la imagen de openjdk que ya viene lista para correr aplicaciones Java (https://hub.docker.com/_/openjdk). Para eso, descargar la imagen con el comando
```bash
$ docker pull openjdk:latest
```

Se puede revisar todas las imágenes descargadas con
```bash
$ docker images
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
docker-network-tutorial_node-server    latest              a86eef18e5d5        15 hours ago        943MB
docker-network-tutorial_java-server2   latest              48afc4be897c        15 hours ago        497MB
...
```
Así como también buscar con algún patrón específico dependiendo de nuestras necesidades
```bash
$ docker images | grep openjdk
openjdk                              latest              0ce6496aae74        6 days ago          497MB
```

Para hacer una prueba simple, ejecutar el siguiente comando
```
$ docker run -it openjdk:latest
```
Esto generará un contenedor y lo ejecutará, devolviendo una terminal dentro del contenedor (gracias al parámetro *-it*). Aquí se pueden escribir sentencias válidas de Java y ejecutarlas, por ejemplo
```java
System.out.println("Hola mundo");
```
Se puede salir del contenedor (y detenerlo) con CTRL+D. Es posible ver el contenedor generado por el comando *docker run* con
```bash
$ docker container ps --all
```
Notar que sin *--all* sólo se verían los contenedores corriendo actualmente.

Si se quiere detener todos los contenedores se puede utilizar el siguiente comando combinado
```bash
$ docker stop $(docker ps -a -q)
```
Si al mismo tiempo, se quiere eliminar todos los contenedores se puede utilizar el siguiente comando combinado
```bash
$ docker rm $(docker ps -a -q)
```
Obviamente, se pueden utilizar los dos comandos al mismo tiempo, a través de un pipeline bash
```bash
$ docker stop $(docker ps -a -q) ; docker rm $(docker ps -a -q)
```

# Sección 3 -- Creando la imagen -- Dockerfile
Ahora es posible usar la imagen descargada para crear nuestra propia imagen, que contendrá la aplicación a ejecutar (en primer lugar, el Servidor). Para esto se generó una estructura de directorios de la siguiente forma:
```
servidor
    |- Dockerfile
    |- src
        |-- Servidor.java
```
En esta parte del tutorial se va a trabajar desde el directorio *servidor*.
*Servidor.java* es una aplicación simple que se pone en escucha en el puerto que indiquemos por argumento, y contesta a quienes se conectan con la hora del servidor.
El archivo a destacar es *Dockerfile*. Aquí es donde se definirán los parámetros para construir la imagen de nuestra aplicación.
En principio está vacío. Con un editor de texto, abrir el archivo y escribir (no copiar) las siguientes líneas

```dockerfile
FROM openjdk:latest
COPY ./src /usr/src/app
WORKDIR /usr/src/app
RUN javac Servidor.java
EXPOSE 4444
CMD ["java", "Servidor", "4444"]
```
Línea a línea, el archivo define:
- FROM: la/s imagen/es a utilizar. Si estas no se encuentran, se descargarán (igual que con *docker pull*)
- COPY: copiar el contenido del directorio *./src* al directorio */usr/src/app* del contenedor. El directorio destino es creado si no existe. Esto nos permite copiar nuestro programa en la imagen generada.
- WORKDIR: cambiar al directorio especificado (igual al comando *cd* en Linux).
- RUN: comando que se ejecuta cuando se construye (build) por primera vez la imagen. En este caso se busca compilar la clase Servidor.java.
- EXPOSE: indica a Docker qué puerto escucha el contenedor.
- CMD: comando que se debe ejecutar cuando el contenedor *se inicia* (distinto a cuando se construye). En este caso se quiere ejecutar la aplicación java, y se le pasa por parámetro el nombre de la clase compilada y un argumento (el puerto).

Con esta configuración ya es posible construir la imagen de la aplicación, a través del siguiente comando
```bash
$ docker build -t tutorial-red-servidor .
```
El parámetro *-t* permite ponerle un nombre a la imagen, en este caso *tutorial-red-servidor*. El punto al final indica que la imagen se debe construir desde el directorio actual (servidor), donde se debe encontrar el Dockerfile.
Con esto, la imagen quedará creada (recordar que se puede revisar con *docker images*).

# Sección 4 -- Creando el contenedor
Una vez creada la imagen, esta se debe instanciar en un contenedor para poder correr la aplicación. Para crear el contenedor se utiliza el comando *docker run*
```bash
$ docker run --name tutorial-red-servidor -p 4444:4444 tutorial-red-servidor:latest
```
*--name* permite definir el nombre del contenedor; en este caso, tutorial-red-servidor
*-p* le indica a Docker que publique el/los puerto/s del contenedor al host, en formato <hostPort>:<containerPort>. Esto permitirá conectarse al contenedor con la dirección *localhost:4444*
Finalmente se define qué imagen utilizar para el contenedor (la que se creó anteriormente).
El contenedor correrá por primera vez y abrirá una terminal con el programa. Si todo funcionó correctamente, el servidor debería estar escuchando en el puerto 4444 y en la terminal debería aparecer un mensaje de bienvenida.
Esto se puede verificar a través de la información que nos da Docker respecto de sus contenedores

```bash
$ docker container ps
CONTAINER ID        IMAGE                          COMMAND                CREATED              STATUS              PORTS                    NAMES
51c8006204de        tutorial-red-servidor:latest   "java Servidor 4444"   About a minute ago   Up About a minute   0.0.0.0:4444->4444/tcp   tutorial-red-servidor
```

Para probar que el servidor funciona correctamente, abrir otra terminal y ejecutar el comando
```bash
$ nc localhost 4444
```

La nueva terminal se conectará al proceso servidor escuchando en el puerto 4444 y responderá con la fecha y hora actual. La terminal que ejecuta el servidor mostrará un log de la conexión.
Para detener el contenedor, presionar CTRL + C.
Finalmente, si se quiere volver a correr el contenedor, ingresar el comando
```bash
$ docker start tutorial-red-servidor -a
```
La opción *-a* de *attach* mostrará la salida estándar y de error en la terminal donde se ejecutó (recordar usar *docker ps --all* para ver todos los contenedores).

# Sección 5 -- Añadiendo persistencia -- Volúmenes
Hasta ahora solamente se tiene acceso a la salida estándar (y errores) de la aplicación montada en el contenedor. Sin embargo, la clase Servidor también genera un archivo de logs en el directorio donde se ejecuta la aplicación; dentro del contenedor, este sería en */usr/src/app*. ¿Cómo se puede acceder a este directorio desde el host?
Una facilidad que ofrece Docker son los volúmenes. Utilizando volúmenes es posible persistir los cambios generados dentro del contenedor. Para esto, se crea un volumen
```bash
$ docker volume create servidor-log
```
Este comando crea el volumen servidor-log. Los volúmenes creados pueden verse con
```bash
$ docker volume ls
```
Y para ver las características particulares del volumen recién creado.  Nótese el atributo "Mountpoint". Este es el directorio *en el host* donde se podrá acceder a los datos generados dentro del contenedor. Con el volumen creado, se necesita crear de nuevo el contenedor con el volumen asignado.

```bash
$ docker volume inspect servidor-log
[
    {
        "CreatedAt": "2020-04-21T11:10:35-03:00",
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/servidor-log/_data",
        "Name": "servidor-log",
        "Options": {},
        "Scope": "local"
    }
]

```
Ahora vamos a volver a correr nuestro contenedor, pero agregando al mismo el volumen previamente definido para mantener persistencia de los datos que deseemos
```bash
$ docker run --name tutorial-red-servidor -v servidor-log:/usr/src/app -p 4444:4444 tutorial-red-servidor:latest
```
En el comando *-v* se especifica <nombre del volumen>:<ruta al directorio que se va a persistir>. En este caso, y como está definido en el Dockerfile, se persiste el directorio raíz de la aplicación.
Si ahora se accede al directorio definido en *Mountpoint*, se podrá leer el archivo de log (dos opciones)
```bash
$ sudo less +F /var/lib/docker/volumes/servidor-log/_data/info.log
$ sudo tail -f /var/lib/docker/volumes/servidor-log/_data/info.log
```
Otro uso útil para los volúmenes es montar un directorio del host dinámicamente en un contenedor. De esta manera, los cambios realizados en el host se actualizan en tiempo real en el contenedor. Por ejemplo, se podría editar y compilar el código fuente en Servidor.java en el host y este se actualizaría en el contenedor sin necesidad de volver a construir la imagen.
Para este ejemplo:
 - Probar con quitar la línea *RUN* en el Dockerfile
 - Compilar en el host (*javac Servidor.java*)
 - Construir la imagen nuevamente (*docker build -t tutorial-red-servidor-v2*)
 - Correr el contenedor con la opción *-v <ruta absoluta al proyecto>:<ruta del contenedor>*
 ```
$ docker run --name tutorial-red-servidor-v2 -v /tmp/:/usr/src/app -p 4444:4444 tutorial-red-servidor-v2:latest
```
Como última aclaración, cabe mencionar que los volúmenes pueden ser accedidos remotamente, compartidos entre contenedores y otras opciones más (consultar https://docs.docker.com/storage/volumes/)

Una vez utilizado, se puede eliminar con el comando rm.  Este comando rm es aplicable a todos los recursos docker (contenedores, redes, almacenamiento, etc). 
```bash
$ docker container rm tutorial-red-servidor
```

# Sección 5 -- Redes Básicas -- Docker compose
Análogamente al caso del servidor, generar un Dockerfile para el Cliente que va a preguntar la fecha y hora al servidor. Como parámetro adicional, el Cliente requiere un nombre de host, por lo que se requiere cambiar la línea *CMD*
```dockerfile
CMD ["java", "Cliente", "server", "4444"]
```
Como no se sabe de antemano la dirección del host (ya que se creará una red nueva con Compose), por ahora se deja *server*.
De esta manera, ahora existen dos aplicaciones en dos contenedores distintos que requieren verse. Aquí entra en juego Docker Compose.
Docker Compose es una herramienta para definir y correr aplicaciones Docker multi contenedor. Este requiere de un archivo *docker-compose.yml* en la raíz del proyecto (en nuestro caso, *tutorial*), que contenga la configuración de los *servicios* que requiere la aplicación. Con esto, basta un solo comando para crear y correr todos los servicios/contenedores definidos.
La estructura de directorios entonces queda:
```
tutorial
    |- docker-compose.yml
    |- cliente
        |-- Dockerfile
        |-- src
            |--- Cliente.java
    |- servidor
        |-- Dockerfile
        |-- src
            |--- Servidor.java
    |- nodejs
        |-- webserver.js
        |-- Dockerfile
```
El archivo de configuración *docker-compose.yml* para la aplicación será el siguiente:
```yaml
version: '3'
services:
  server:
    build: ./servidor
    ports:
      - "4444:4444"
  client:
    build: ./cliente
```
Aquí se declaran
- *version*: la versión del formato de configuración (generalmente 3, la última)
- *services*: los servicios que requiere nuestra aplicación. 
-- un contenedor *server* que se construirá con la imagen generada por el directorio *./servidor*, y que publicará los puertos de manera similar al argumento *-p* en *docker run*.
-- un contenedor *client* que hará lo mismo con la imagen en *./cliente*

Para correr este ejemplo, se ejecuta la siguiente línea, desde el directorio raíz (*tutorial*):
```bash
$ docker-compose up
```
Con esto, Docker Compose se encargará de:
- Crear las imágenes
- Correr los contenedores

Luego, mostrará en la terminal la salida de ambos contenedores. Pero queda un asunto sin cerrar. ¿Cómo puede el cliente ver al servidor sin definir el host?
En el Dockerfile del cliente se definió como argumento para host (en *CMD*) la palabra "server". Y eso es todo lo que se necesita para que dos contenedores se vean, que conozcan sus *nombres de servicio* declarados en *docker-compose.yml*. Así, el cliente se conecta a server:4444, Docker resuelve esa dirección y dirige los datos al servidor.
Docker compose provee muchas herramientas avanzadas para hacer deploy y comunicar contenedores y servicios de todo tipo. Para más información dirigirse a la documentación de Docker Compose (https://docs.docker.com/compose/)

## Sección 6 -- Conectarse por SSH a un contenedor y debug de arquitectura
Otra actividad importante sobre la que trabajaremos brevemente en este tutorial inicial es la revisión "detallada" de un contenedor o de un conjunto de contenedores (estado de servicios, chequeos de configuración, puertos, entre otras cosas).  Para ello, además de los controles que se pueden realizar desde el HOST (Anfitrión) que aloja Docker, es posible conectarse a las instancias y analizar "internamente" que sucede. En ambientes con productos y servicios en ejecución no siempre es posible cambiar algo y relanzar el contenedor.  En ese caso también es una herramienta util.

Como punto de partida, entonces, necesitamos acceder a la consola BASH del contenedor y a partir de ahí comenzar a realizar las tareas de administración tradicionales que se pueden realizar sobre una distribución linux.  Tener en cuenta que las imágenes Docker tienden a ser mucho más pequeñas que una imagen completa de Debian ; Ubuntu o similares.  Por dicho motivo, puede ser requisito instalar la mayoría de los paquetes que se vayan a utilizar para la revisión de los servicios.

Para continuar trabajando con los archivos docker-compose, vamos a agregar a la red que construimos previamente un nodo básico de debian, de la siguiente manera.
Archivo: docker-compose-ssh-debug.yml
```yaml
version: '3'
services:
  server:
    build: ./servidor
    ports:
      - "4444:4444"
  server2:
    build: ./servidor
    ports:
      - "4443:4444"
  nginx-webserver:
    image: nginx:latest
```

A continuación, debemos ponerlo a correr.  De esta manera se levantarán dos servidores (nombre server y server2) y va a levantar una imagen de nginx básica a través del repositorio de Docker Hub (Imagen debian oficial) a la que nos vamos a conectar por BASH para revisar sus configuraciones y el entorno de estos servicios asociados
```bash
$ docker-compose -f docker-compose-ssh-debug.yml up
Starting docker-network-tutorial_server_1          ... done
Starting docker-network-tutorial_server2_1         ... done
Creating docker-network-tutorial_nginx-webserver_1 ... done
Attaching to docker-network-tutorial_server_1, docker-network-tutorial_server2_1, docker-network-tutorial_nginx-webserver_1
server_1           | [Wed Apr 22 14:34:01 GMT 2020] INFO Servidor iniciado en puerto 4444
server2_1          | [Wed Apr 22 14:34:01 GMT 2020] INFO Servidor iniciado en puerto 4444
```

En otra terminal o pestaña de terminal, sin parar los servicios que pusimos a correr previamente, vamos a revisar que los 3 contenedores estén corriendo correctamente a través del siguiente comando:
```bash
$ docker container ps 
CONTAINER ID        IMAGE                             COMMAND                  CREATED              STATUS              PORTS                    NAMES
a8df97dd5cf1        nginx:latest                      "nginx -g 'daemon of…"   13 seconds ago       Up 12 seconds       80/tcp                   docker-network-tutorial_nginx-webserver_1
ca55452b51e8        docker-network-tutorial_server2   "java Servidor 4444"     About a minute ago   Up 12 seconds       0.0.0.0:4443->4444/tcp   docker-network-tutorial_server2_1
6875ccf07909        docker-network-tutorial_server    "java Servidor 4444"     About a minute ago   Up 12 seconds       0.0.0.0:4444->4444/tcp   docker-network-tutorial_server_1
```

Vamos a analizar (inspeccionar) las propiedades de los contenedores (lo vamos a ver en uno pero aplicable al resto) para obtener información de red de los mismos. La información es muy detallada, solo voy a mostrar en el tutorial lo relevante para este apartado.  Para ello, vamos a ejecutar el siguiente comando.
```bash
$ docker container inspect a8df97dd5cf1
[
    {
        "Id": "a8df97dd5cf17564f4d2cc6c364f23bad0451cd18b2e2f424d45a51b84026648",
        "Created": "2020-04-22T14:34:00.538018261Z",
        "Path": "nginx",
...
    "State": {
            "Status": "running",
...
    "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "NGINX_VERSION=1.17.10",
                "NJS_VERSION=0.3.9",
                "PKG_RELEASE=1~buster"
            ],
...
    "Image": "nginx:latest",
            "Volumes": null,
            "WorkingDir": "",
...
    "NetworkSettings": {
       "docker-network-tutorial_default": {
...
        "Ports": {
                "80/tcp": null
            },
...
      "Gateway": "172.18.0.1",
      "IPAddress": "172.18.0.4",
       "IPPrefixLen": 16,
...
]
```
Esto, entonces, me permite ver las propiedades más importantes (y detalladas) de nuestro contenedor.  Ahora sabemos que la dirección IP es 172.18.0.4, que la imagen corriendo es NGINX y está escuchando en el puerto 80.  También podemos determinar que esa dirección IP pertenece al a red "docker-network-tutorial_default".
Podemos completar esta información con lo que podemos obtener analizando la red a la que el contenedor pertenece a través del siguiente comando.

```bash
$ docker network inspect docker-network-tutorial_default
docker network inspect docker-network-tutorial_default
[
    {
        "Name": "docker-network-tutorial_default",
 ...
        "Scope": "local",
        "Driver": "bridge",
 ...
            "Config": [
                {
                    "Subnet": "172.18.0.0/16",
                    "Gateway": "172.18.0.1"
                }
            ]
        },
 ...
        "Containers": {
            "6875ccf079096d899d83771cac971d32e17ec121383815495bbd3ee6be0d57f0": {
                "Name": "docker-network-tutorial_server_1",
                "EndpointID": "7240616da52f2eb7e7721795a10398260f305484ff43197dc98b2d358e7ab99b",
                "MacAddress": "02:42:ac:12:00:02",
                "IPv4Address": "172.18.0.2/16",
                "IPv6Address": ""
            },
            "a8df97dd5cf17564f4d2cc6c364f23bad0451cd18b2e2f424d45a51b84026648": {
                "Name": "docker-network-tutorial_nginx-webserver_1",
                "EndpointID": "4080b6f1deb91953681d6e7543aadd74e220b7a617a8a34da3635173d182c623",
                "MacAddress": "02:42:ac:12:00:04",
                "IPv4Address": "172.18.0.4/16",
                "IPv6Address": ""
            },
            "ca55452b51e8d3c0b18b83cc68131a72789eb194fcf1d67f0927ef3e678b1a69": {
                "Name": "docker-network-tutorial_server2_1",
                "EndpointID": "5c48fa60c720555c1f559fa134171105f5f1d59b3acc0368cea70eeb340043f4",
                "MacAddress": "02:42:ac:12:00:03",
                "IPv4Address": "172.18.0.3/16",
                "IPv6Address": ""
            }
   ...
]
```
De esta manera, ahora sabemos también las direcciones IP de los otros contenedores que están corriendo (los de nuestro docker-compose-ssh.yml)

Ahora, habiendo realizado las verficaciones necesarias, vamos a conectarnos a la consola de /bin/bash del contenedor de nginx (nginx-webserver) simulando un "SSH" al contenedor a través del siguiente comando para realizar algunas pruebas "desde adentro" de la red de los contenedores. 

```bash
$ docker exec -it a8df97dd5cf1 /bin/bash
root@a8df97dd5cf1:/# 
```
Donde "exec" significa el comando que se va a ejecutar en el contenedor en ejecución.  El comando exec solo se ejecutará mientras se está ejecutando el proceso primario del contenedor (PID 1).  La estructura será la siguiente
```bash
docker exec -ti my_container sh -c "echo a && echo b".
```
El parámetro "-it" permitirá levantar una terminal interactiva (en vez de ejecutar un comando) para que podamos acceder a la consola del contenedor. Específicamente el último parámetro "/bin/bash" indicará al contenedor que la consola que queremos utilizar será bash (ya que podría haber otras disponibles cómo sh, dash, entre otras)
Para más información acerca de la ejecución de comandos dentro de la consola, dirigirse al sitio oficial de docker (https://docs.docker.com/engine/reference/commandline/exec/)

Entonces, Una vez dentro, podemos realizar diversos comandos básicos para ver el estado y los paquetes instalados en dicha distribución.
Recuerden que siempre se va a tratar de instalar la menor cantidad posible de paquetes para:
a) No incrementar el tamaño del contenedor
b) Al instalar un paquete en un contenedor corriendo (en base a una imagen) los cambios solo quedarán reflejados en este contenedor, no en la imagen base.  Por lo tanto si despliego una nuevo contenedor basado en la imagen fuente, los contenedores no serán iguales.
c) No habilitar herramientas innecesarias que pueden dañar el resto de la red de contenedores (Imágenes root, SSH client, etc)

* Vamos a comenzar por averiguar el sistema y versión de nuestro sistema operativo.
```bash
root@a8df97dd5cf1:/# uname -a
Linux a8df97dd5cf1 4.19.0-8-amd64 #1 SMP Debian 4.19.98-1 (2020-01-26) x86_64 GNU/Linux

root@a8df97dd5cf1:/# cat /etc/os-release
PRETTY_NAME="Debian GNU/Linux 10 (buster)"
NAME="Debian GNU/Linux"
VERSION_ID="10"
VERSION="10 (buster)"
VERSION_CODENAME=buster
ID=debian
HOME_URL="https://www.debian.org/"
SUPPORT_URL="https://www.debian.org/support"
BUG_REPORT_URL="https://bugs.debian.org/"
root@a8df97dd5cf1:/# 
```

* Bien, ahora la intención es validar si dentro de la red docker que estemos (esto depende de como se haya generado el docker-compose file) los contenedores se pueden ver por ejemplo a través de IP y nombre de DNS (y buscar de donde viene el nombre de DNS)
```bash
root@a8df97dd5cf1:/# ping
bash: ping: command not found
```
Cómo podemos ver, los paquetes "necesarios" no están disponibles.  Para ello vamos a tener que instalarlos.  A continuación se definen un conjunto de herramientas que se consideran necesarias para revisar las tareas que queremos llevar a cabo. 
```bash
root@a8df97dd5cf1:/# apt update -y; apt install iputils-ping -y; apt install net-tools -y; apt install telnet -y; apt install ssh-client -y; apt install netcat -y; apt install nmap -y
...
update-alternatives: using /bin/nc.traditional to provide /bin/nc (nc) in auto mode
update-alternatives: warning: skip creation of /usr/share/man/man1/nc.1.gz because associated file /usr/share/man/man1/nc.traditional.1.gz (of link group nc) doesn't exist
update-alternatives: warning: skip creation of /usr/share/man/man1/netcat.1.gz because associated file /usr/share/man/man1/nc.traditional.1.gz (of link group nc) doesn't exist
Setting up netcat (1.10-41.1) 
...
```

* Una vez descargado los paquetes, primero que nada voy a revisar mi dirección IP
```bash
root@a8df97dd5cf1:/# ifconfig | grep 172.
inet 172.18.0.4  netmask 255.255.0.0  broadcast 172.18.255.255
```
Lo que condice con la información que nos decia el container y la docker-network.

* También voy a revisar que se cumple lo que dice el docker container ps (PORTS tcp/80). Para ello ejecutamos lo siguiente
```bash
root@a8df97dd5cf1:/# netstat -ant 
Active Internet connections (servers and established)
Proto Recv-Q Send-Q Local Address           Foreign Address         State      
tcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN     
tcp        0      0 127.0.0.11:40377        0.0.0.0:*               LISTEN     
```
* A continuación vamos a intentar hacer ping a nuestros vecinos (por IP y por nombre de DNS), para ello utilizamos el paquete ping
```bash
root@a8df97dd5cf1:/# ping 172.18.0.3
PING 172.18.0.2 (172.18.0.3) 56(84) bytes of data.
64 bytes from 172.18.0.3: icmp_seq=1 ttl=64 time=0.101 ms
64 bytes from 172.18.0.3: icmp_seq=2 ttl=64 time=0.190 ms
...
root@a8df97dd5cf1:/# ping server2        
PING server2 (172.18.0.3) 56(84) bytes of data.
64 bytes from docker-network-tutorial_server2_1.docker-network-tutorial_default (172.18.0.3): icmp_seq=1 ttl=64 time=0.094 ms
64 bytes from docker-network-tutorial_server2_1.docker-network-tutorial_default (172.18.0.3): icmp_seq=2 ttl=64 time=0.317 ms
...
```
* También vamos a investigar que puertos tiene abierto un determinado nodo de la red (Ya que no tenemos habilitadas reglas de seguridad en los nodos)
```bash
root@a8df97dd5cf1:/# nmap server2
Starting Nmap 7.70 ( https://nmap.org ) at 2020-04-22 22:32 UTC
Nmap scan report for server2 (172.18.0.4)
Host is up (0.000034s latency).
rDNS record for 172.18.0.4: docker-network-tutorial_server2_1.docker-network-tutorial_default
Not shown: 999 closed ports
PORT     STATE SERVICE
4444/tcp open  krb524
MAC Address: 02:42:AC:12:00:04 (Unknown)

Nmap done: 1 IP address (1 host up) scanned in 1.65 seconds
```
* Vamos a validar que podemos acceder a la información del servidor de hora que se encuentra escuchando en el 4444 en alguno de los servidores

```bash
root@a8df97dd5cf1:/# nc server2 4444
Bienvenido al servidor de fecha y hora
Wed Apr 22 22:37:52 GMT 2020
```

Finalmente parar los servicios definidos para finalizar con esta parte del tutorial (consola que tiene el docker-compose up). 
Ahora, tomar un descanso y estar listos para [la segunda parte del tutorial](https://github.com/dpetrocelli/sdypp2020/tree/master/TPS/No%20obligatorios/docker-network-tutorial-p2)

Gracias!
