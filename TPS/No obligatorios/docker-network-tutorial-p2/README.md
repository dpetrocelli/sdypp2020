# Tutorial parte 2 -- Escalar servicios en Docker con Docker compose
## Sección 1 -- Introducción

Bienvenidos a la segunda parte del tutorial de Docker network. Este tutorial te guiará en:
- Continuar avanzando con los conceptos para trabajar con tus propias imágenes
- Continuar configurando contenedores utilizando Dockerfiles
- Trabajar con volúmenes compartidos (en Host) para persistir elementos en contenedores y actualizar cambios
- Comprender como pasar parámetros a los contenedores (ajustando el código java) desde Docker compose
- Diseñar e implementar servicios distribuidos sobre Docker 
- Implementar balanceo de cargas en el Host para redireccionar peticiones a los servicios distribuidos
- Utilizar imágenes prearmadas de RabbitMQ para levantar el middleware de colas
- Persistir los datos de la instancia de RabbitMQ 
- Construir un cluster de RabbitMQ para brindar alta disponibilidad, redundancia y tolerancia a fallas
- Definir políticas (HA Policies de RabbitMQ) para crear colas con persistencia (durable) y replicas (ha:mode)
- Trabajar con simulación de fallas en nodos RabbitMQ 
- Integrar la administración desde Java hacia la API de RabbitMQ 
- Integrar balance de carga para optimizar las tareas sobre los nodos de RabbitMQ
- Integrar un balance de carga con alta disponibilidad con HAProxy y KeepAlived para evitar un único punto de fallo en el sistema (VER)

## Sección 2 -- Manejo de parámetros en Docker compose
Existen varias maneras para pasar parámetros a las aplicaciones desde DockerCompose.
En este caso nosotros utilizaremos las variables de entorno (environment).
En este caso el código del servidor de Java se adaptó para que puedan tomarse dichos parámetros desde las variables de entorno.
* Cambios en código Java
```java
String nombre = System.getenv("nombre");
String logfile = System.getenv("logName");
int port = Integer.parseInt(System.getenv("port"));
```
* Cambios en yml docker-compose
```yaml
version: '3'
services:
  server:
    build: ./servidor
    ports:
      - "4444:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
      - nombre=Servidor 1
      - logName=logfile1
      - port=4444
```
* El Dockerfile para crear la imágen de nuestro servicio se mantiene igual
Nombre de archivo: docker-compose-para-java.yml
```yaml
FROM openjdk:latest
COPY ./src /usr/src/app
WORKDIR /usr/src/app
RUN javac Servidor.java
EXPOSE 4444
CMD ["java", "Servidor"]
```
- Una vez revisado los tres pasos, se pone a correr la instancia para verificar que recibe los parámetros definidos


```bash
$ docker-compose -f docker-compose-para-java.yml up
```

- En la salida podemos verificar que el servidor levanta en base a los parámetros definidos
>Starting docker-network-tutorial-p2_server_1 ... done
>Attaching to docker-network-tutorial-p2_server_1
>server_1  | [Mon Apr 13 23:24:15 GMT 2020] INFO Servidor 1 iniciado en puerto 4444

- Desde otra terminal podemos realizar un netcat para ver lo que nos brinda
```bash
nc localhost 4444
```
Dando como resultado algo similar a
>Bienvenido al servidor de fecha y hora Servidor 1
>Mon Apr 13 23:25:41 GMT 2020

Mientras que el servicio en la terminal anterior (servidor) nos muestra algo similar a:
> server_1  | [Mon Apr 13 23:25:41 GMT 2020] INFO Cliente conectado /172.22.0.1:59434

- Corroborar que también se almacenan los datos del log en el volumen definido
```bash
cat /tmp/javadir/logfile1.log
```
>[Mon Apr 13 23:24:15 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
>[Mon Apr 13 23:25:41 GMT 2020] INFO Cliente conectado /172.22.0.1:59434
>[Mon Apr 13 23:54:09 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
>[Tue Apr 14 00:14:31 GMT 2020] INFO Servidor 1 iniciado en puerto 4444

## Sección 3 -- Crear varias instancias para un mismo servicio
El objetivo es crear múltiples instancias independientes del servicio Java definido para brindar mayor disponibilidad del servicio.
Obviamente, en la sección 4, se verá como integrar esto con un balanceador de carga para explotar estas características.

Para cumplir con este objetivo se debe ajustar el archivo yaml para que en vez de levantar una instancia, ponga a correr N de acuerdo a las necesidades del usuario.  Por ejemplo, de la siguiente manera:
Nombre de archivo: docker-compose-java-escalado.yml
```yaml
version: '3'
services:
  server:
    build: ./servidor
    ports:
      - "4444:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
      - nombre=Servidor 1
      - logName=logfile1
      - port=4444
  server2:
    build: ./servidor
    ports:
      - "4443:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
        - nombre=Servidor 2
        - logName=logfile2
        - port=4444 
```
**Nota: Prestar especial atención en la definición de "ports:" ya que en el host (es decir su equipo) no puede haber más de un puerto asignado en la misma IP (definición de TCP/IP).** 
Por lo tanto, en este ejemplo dice: 
```yaml
ports:
      - "4443:4444"
```
Lo que significa que: 
- En el host el puerto de escucha será el 4443 (en vez del original)
- En la aplicación interna el puerto seguirá siendo el mismo (4444)
- Docker hará un bind entre el puerto host y puerto de la aplicación redireccionando todo lo que viene al 4443 hacia el 4444
En este caso, docker mostrará: 0.0.0.0:4443->4444/tcp 
Siendo esto obtenido desde ejecutar
```bash
$ docker container ps
```
viendo la columna ports.

### A modo de resumen se presentan los resultados:

- Correr el docker compose
```bash
$ docker-compose -f docker-compose-java-escalado.yml up
```
>Starting docker-network-tutorial-p2_server2_1 ... done
>Starting docker-network-tutorial-p2_server3_1 ... done
>Starting docker-network-tutorial-p2_server_1  ... done
>Attaching to docker-network-tutorial-p2_server_1, docker-network-tutorial-p2_server2_1, docker-network-tutorial-p2_server3_1
>server_1   | [Mon Apr 13 23:54:09 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
>server2_1  | [Mon Apr 13 23:54:10 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
>server3_1  | [Mon Apr 13 23:54:10 GMT 2020] INFO Servidor 3 iniciado en puerto 4444

- Revisar como están los contenedores y puertos (prestar atención a los bind de los puertos)
```bash
$ docker container ps
```
>CONTAINER ID        IMAGE                                COMMAND             CREATED             STATUS              PORTS                    NAMES
>90bff8a1b185        docker-network-tutorial-p2_server3   "java Servidor"     22 hours ago        Up 2 seconds        0.0.0.0:4442->4444/tcp   docker-network-tutorial-p2_server3_1
>bad75e5d8dac        docker-network-tutorial-p2_server2   "java Servidor"     22 hours ago        Up 2 seconds        0.0.0.0:4443->4444/tcp   docker-network-tutorial-p2_server2_1
>2609e17074ce        docker-network-tutorial-p2_server    "java Servidor"     22 hours ago        Up 2 seconds        0.0.0.0:4444->4444/tcp   docker-network-tutorial-p2_server_1

- Probar con netcat el resultado de los servidores
```bash
$ netcat localhost 4443
```
>Bienvenido al servidor de fecha y hora Servidor 2
>Tue Apr 14 00:16:47 GMT 2020

- Corroborar que también se almacenan los datos del log en el volumen definido
```bash
$ cat /tmp/javadir/logfile2.log
```
>[Mon Apr 13 23:54:10 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
>[Tue Apr 14 00:14:31 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
>[Tue Apr 14 00:16:47 GMT 2020] INFO Cliente conectado /172.22.0.1:53830

## Sección 4 -- Balancear carga entre instancias de servicios con HaProxy
Para balancear carga entre los servicios de Java se va a proceder a instalar un balanceador de carga en el Host (equipo). Para ello se va a instalar HaProxy a través de la línea de comandos y los repositorios APT.

```bash
$ sudo apt update ; sudo apt install haproxy
```

* Una vez instalado se va a proceder a configurar el balanceador en base a la arquitectura definida.  Como archivo de configuración en sistema Linux se encuentra dentro del directorio etc.  En este caso en el archivo se encuentra en /etc/haproxy/haproxy.cfg

- Primero, realizar un bkp del archivo original
```bash
$ cp /etc/haproxy/haproxy.cfg /etc/haproxy/haproxy_old.cfg
```
- Segundo, agregar al final del contenido del archivo la siguiente información
```bash
$ vim /etc/haproxy/haproxy.cfg
...
listen backend-server
    #Peticiones que lleguen a 8080
    bind 0.0.0.0:8080
    mode tcp
    #Lo balanceo con mismo peso (RoundRobin)
    balance roundrobin
    server backend-server1 127.0.0.1:4444 check fall 3 rise 2
    server backend-server2 127.0.0.1:4443 check fall 3 rise 2
    server backend-server3 127.0.0.1:4442 check fall 3 rise 2

listen stats
    #Interfaz de administracion/visualizacion
    bind 0.0.0.0:8181
    stats enable
    stats uri /
    stats realm Haproxy\ Statistics
    stats auth admin:admin
    stats refresh 2s
```
- Tercero, reinicio (o recargo) el servicio para que aplique las configuraciones definidas. 
Obviamente, el comando dependerá de la distribución linux

```bash
$ sudo systemctl restart/reload haproxy
```

- Cuarto, verifico que el balanceador esté corriendo sin problemas

```bash
$ sudo systemctl status haproxy
```
Donde debe dar algo similar a lo siguiente
>Active: active (running) since Mon 2020-04-13 22:07:10 -03; 1s ago

- Quinto, iniciar el sitio de administración (http://localhost:8181) y validar que estén los nodos en verde funcionando.

- Sexto, realizar pruebas de peticiones y validar que la carga se va repartiendo entre los nodos (a la configuración del bind del haproxy)

```bash
$ telnet localhost 8080 & telnet localhost 8080 & telnet localhost 8080 & telnet localhost 8080
```
- Finalmente, luego de validar las funciones, parar los servicios.

## Sección 5 -- Crear un cluster RabbitMQ con Docker Compose

### 5.1 - Qué es RabbitMQ?

> RabbitMQ es un "open source message broker software" que implementa los protocolos de "Advanced Message Queuing Protocol (AMQP)".
> El servicio de RabbitMQ está escrito en Erlang y construido por Open Telecom Platform
> Tiene la capacidad de construir su servicio como un cluster con capacidad de redundancia ante fallas. Están disponibles  
> API's para varios lenguajes de programación

https://www.rabbitmq.com/

### 5.2 - RabbitMQ en Docker
La imagen oficial de RabbitMQ se encuentra desarrollada y presentada (desde Marzo de 2020) por Bitnami Inc y se encuentra preparada para trabajar en diversos entornos (local, Cloud, Kubernetes, etc)
https://bitnami.com/stack/rabbitmq/containers

La imagen base se encuentra en Docker Hub, donde se puede visualizar el contínuo update de la misma. 
https://hub.docker.com/r/bitnami/rabbitmq/

### 5.3 - Porqué usar las imágenes Bitnami

* Bitnami monitorea contínuamente los cambios que producen los proveedores de las plataformas y publica rápidamente las nuveras versiones de esas imágenes a través de pipelines automatizados 
* Con las imágenes de Bitnami, las últimas correcciones de errores y características están disponibles lo antes posible.
* Los contenedores de Bitnami, las máquinas virtuales y las imágenes en la nube utilizan los mismos componentes y el mismo enfoque de configuración, lo que facilita el cambio entre formatos según las necesidades de su proyecto.
* Todas las imágenes de bitnami están basadas en [minideb] (https://github.com/bitnami/minideb) que es una imagen de contenedor minimalista de tamaño reducido basada en Debian (distribución líder de Linux) .
* Todas las imágenes de Bitnami disponibles en Docker Hub están firmadas con [Docker Content Trust (DTC)] (https://docs.docker.com/engine/security/trust/content_trust/). Puede usar `DOCKER_CONTENT_TRUST = 1` para verificar la integridad de las imágenes

Cómo siempre existen varias maneras de crear un cluster de un determinado servicio para ofrecer características de redundancia, tolerancia a fallos y replicación. Especialmente en docker existen siempre diversos usuarios que publican adaptaciones de las imágenes oficiales para funciones específicas o facilitar el uso de algún plugin o herramienta.  Sin embargo, este tipo de adaptaciones casi siempre quedan sin actualizaciones frecuentes y terminan ofreciendo herramientas con ciertas deficiencias.  Por lo tanto, en este tutorial siempre se busca utilizar herramientas de repositorios oficiales con updates contínuos.

### 5.4 - Cómo correr RabbitMQ desde la imagen básica (docker) de Bitnami

* Obtener la imagen desde el repositorio
```bash
$ docker run --name rabbitmq bitnami/rabbitmq:latest

Unable to find image 'bitnami/rabbitmq:latest' locally
latest: Pulling from bitnami/rabbitmq
f741ee3cf64f: Pull complete 
6e1a7737956d: Pull complete 
ced5f76dedf2: Downloading [=============================================>     ]  14.34MB/15.83MB
....

Starting RabbitMQ 3.8.3 on Erlang 22.3
 Copyright (c) 2007-2020 Pivotal Software, Inc.
 Licensed under the MPL 1.1. Website: https://rabbitmq.com

  ##  ##      RabbitMQ 3.8.3
  ##  ##
  ##########  Copyright (c) 2007-2020 Pivotal Software, Inc.
.....
2020-04-13 15:22:53.106 [info] <0.337.0> Running boot step networking defined by app rabbit
2020-04-13 15:22:53.108 [info] <0.488.0> started TCP listener on [::]:5672
2020-04-13 15:22:53.108 [info] <0.337.0> Running boot step cluster_name defined by app rabbit
2020-04-13 15:22:53.108 [info] <0.337.0> Running boot step direct_client defined by app rabbit
2020-04-13 15:22:53.109 [info] <0.337.0> Running boot step os_signal_handler defined by app rabbit
2020-04-13 15:22:53.109 [info] <0.490.0> Swapping OS signal event handler (erl_signal_server) for our own
2020-04-13 15:22:53.149 [info] <0.540.0> Management plugin: HTTP (non-TLS) listener started on port 15672
2020-04-13 15:22:53.149 [info] <0.645.0> Statistics database started.
.....
```

* Verificar (en otra terminal) que está corriendo el contenedor
```bash
$ docker container ps
```
```
CONTAINER ID        IMAGE                     COMMAND                  CREATED             STATUS              PORTS                                      NAMES
7daa75d3e3db        bitnami/rabbitmq:latest   "/opt/bitnami/script…"   12 seconds ago      Up 10 seconds       4369/tcp, 5672/tcp, 15672/tcp, 25672/tcp   rabbitmq
```
* Parar el servicio (Ctrl + c)

#### 5.5 - Usar Docker Compose para correr RabbitMQ 

* Descargar el "template" del docker-compose disponible por el proveedor y visualizarlo 

```bash
$ curl -sSL https://raw.githubusercontent.com/bitnami/bitnami-docker-rabbitmq/master/docker-compose.yml > docker-compose.yml
cat docker-compose.yml
```
```yaml
version: '3'

services:
  rabbitmq:
    image: 'bitnami/rabbitmq:latest'
    ports:
      - '4369:4369'
      - '5672:5672'
      - '25672:25672'
      - '15672:15672'
    volumes:
      - 'rabbitmq_data:/bitnami'
volumes:
  rabbitmq_data:
    driver: local
```

* Hacer una prueba y levantarlo con docker-compose
```bash
$ docker-compose up -d
```

#### 5.6 - Persistir los datos de RabbitMQ (a pesar de reinicio) 

Si se elimina el contenedor, todos sus datos se perderán, y la próxima vez que ejecute la imagen, la base de datos se reiniciará. Para evitar esta pérdida de datos, debe montar un volumen que persistirá incluso después de quitar el contenedor.

Para persistencia, debe montar un directorio en la ruta `/bitnami`. Si el directorio montado está vacío, se inicializará en la primera ejecución.

```bash
$ docker run \
    -v /path/to/rabbitmq-persistence:/bitnami \
    bitnami/rabbitmq:latest
```

También puede hacer esto con un cambio menor en el archivo [`docker-compose.yml`] (https://github.com/bitnami/bitnami-docker-rabbitmq/blob/master/docker-compose.yml) presente en este repositorio:

```yaml
rabbitmq:
  ...
  volumes:
    - /path/to/rabbitmq-persistence:/bitnami
  ...
```

#### 5.7 - Configuración avanzada de RabbitMQ (Docker)

##### Variables de entorno (Como vimos al principio)

Cuando se inicia la imagen de rabbitmq, se pueden ajustar configuraciones de la instancia pasando una o más variables de entorno en el archivo docker-compose o en la línea de comandos de ejecución de docker. Si desea agregar una nueva variable de entorno:

Para docker-compose, agregue el nombre y el valor de la variable en la sección de la aplicación en [`docker-compose.yml`] 

```yaml
rabbitmq:
  ...
  environment:
    - RABBITMQ_PASSWORD=my_password
  ...
```
* Variables disponibles:

 - `RABBITMQ_USERNAME`: RabbitMQ application username. Default: **user**
 - `RABBITMQ_PASSWORD`: RabbitMQ application password. Default: **bitnami**
 - `RABBITMQ_HASHED_PASSWORD`: RabbitMQ application hashed password.
 - `RABBITMQ_VHOST`: RabbitMQ application vhost. Default: **/**
 - `RABBITMQ_ERL_COOKIE`: Erlang cookie to determine whether different nodes are allowed to communicate with each other.
 - `RABBITMQ_NODE_TYPE`: Node Type. Valid values: *stats*, *queue-ram* or *queue-disc*. Default: **stats**
 - `RABBITMQ_NODE_NAME`: Node name and host. E.g.: *node@hostname* or *node* (localhost won't work in cluster topology). Default **rabbit@localhost**. If using this variable, ensure that you specify a valid host name as the container wil fail to start otherwise.
 - `RABBITMQ_NODE_PORT_NUMBER`: Node port. Default: **5672**
 - `RABBITMQ_CLUSTER_NODE_NAME`: Node name to cluster with. E.g.: **clusternode@hostname**
 - `RABBITMQ_CLUSTER_PARTITION_HANDLING`: Cluster partition recovery mechanism. Default: **ignore**
 - `RABBITMQ_MANAGER_PORT_NUMBER`: Manager port. Default: **15672**
 - `RABBITMQ_DISK_FREE_LIMIT`: Disk free space limit of the partition on which RabbitMQ is storing data. Default: **{mem_relative, 1.0}**
 - `RABBITMQ_ULIMIT_NOFILES`: Resources limits: maximum number of open file descriptors. Default: **65536**
 - `RABBITMQ_ENABLE_LDAP`: Enable the LDAP configuration. Defaults to `no`.
 - `RABBITMQ_LDAP_TLS`: Enable secure LDAP configuration. Defaults to `no`.
 - `RABBITMQ_LDAP_SERVER`: Hostname of the LDAP server. No defaults.
 - `RABBITMQ_LDAP_SERVER_PORT`: Port of the LDAP server. Defaults to `389`.
 - `RABBITMQ_LDAP_USER_DN_PATTERN`: DN used to bind to LDAP in the form `cn=$${username},dc=example,dc=org`. No defaults.

#### 5.8 - Construir un cluster RabbitMQ usando Docker Compose

This is the simplest way to run RabbitMQ with clustering configuration:

##### Paso 1: Crear en /tmp/ los "volúmenes" que usaremos para montar en los nodos 
Vamos a crear:
* 1 folder para el stats+node
* 1 folder para cada queue-disc
```bash
 mkdir /tmp/rabbit; mkdir /tmp/rabbit/stats ; mkdir /tmp/rabbit/node1 ; mkdir /tmp/rabbit/node2 ; mkdir /tmp/rabbit/node3; sudo chmod 777 -R /tmp/rabbit
```
##### Paso 2: Crear el primer nodo stats al docker-compose.yml`

* Copie el código a continuación en su docker-compose.yml para agregar un nodo de estadísticas web RabbitMQ a la configuración de su clúster.

```yaml
version: '3'

services:
  stats:
    image: bitnami/rabbitmq
    environment:
      - RABBITMQ_NODE_TYPE=stats
      - RABBITMQ_NODE_NAME=rabbit@stats
      - RABBITMQ_ERL_COOKIE=s3cr3tc00ki3
    ports:
      - '15672:15672'
    volumes:
      - '/tmp/rabbit/stats:/bitnami'
```

> ** Nota: ** El nombre del servicio (** stats **) es importante para que un nodo pueda resolver el nombre de host para luego poder agrupar los nodos. (Tenga en cuenta que el nombre del nodo es `rabbit@stats`)

* Verificar (además de lo informado por consola) que está corriendo el contenedor
```bash
$ docker container ps

CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                                                  NAMES
223e1587c160        bitnami/rabbitmq    "/opt/bitnami/script…"   39 minutes ago      Up 25 minutes       4369/tcp, 5672/tcp, 25672/tcp, 0.0.0.0:15672->15672/tcp                docker-network-tutorial-p2_stats_1
```

* Validar ingresando a la web-gui que está corriendo el servicio
http://localhost:15672

##### Paso 3: Agregar nodos "colas" a la configuración del cluster RabbitMQ

Actualizar con uno o más nodos según el ejemplo siguiente 

```yaml
version: '3'
services:
  stats:
    image: bitnami/rabbitmq
    environment:
      - RABBITMQ_NODE_TYPE=stats
      - RABBITMQ_NODE_NAME=rabbit@stats
      - RABBITMQ_ERL_COOKIE=s3cr3tc00ki3
    ports:
      - '15672:15672'
    volumes:
      - '/tmp/rabbit/stats/:/bitnami'
  node1:
    image: bitnami/rabbitmq
    environment:
      - RABBITMQ_NODE_TYPE=queue-disc
      - RABBITMQ_NODE_NAME=rabbit@node1
      - RABBITMQ_CLUSTER_NODE_NAME=rabbit@stats
      - RABBITMQ_ERL_COOKIE=s3cr3tc00ki3
    ports:
      - 5672:5672
      - 4369:4369
    volumes:
      - '/tmp/rabbit/node1:/bitnami'
  node2:
    image: bitnami/rabbitmq
    environment:
      - RABBITMQ_NODE_TYPE=queue-disc
      - RABBITMQ_NODE_NAME=rabbit@node2
      - RABBITMQ_CLUSTER_NODE_NAME=rabbit@stats
      - RABBITMQ_ERL_COOKIE=s3cr3tc00ki3
    ports:
      - 5673:5672
      - 4370:4369
    volumes:
      - '/tmp/rabbit/node2:/bitnami'   
```

##### Paso 4: Parar el cluster y verificar las configuraciones 

Detener el cluster utilizando los siguientes comandos de Docker Compose: 

```bash
$ docker-compose stop rabbitmq
```
##### Paso 5: Realizar un backup de las configuraciones para disponer en otro nodo más adelante 
El backup se realizará a través de utilizar la herramienta rsync hacia otra carpeta utilizando

```bash
$ rsync -a /path/to/rabbitmq-persistence /path/to/rabbitmq-persistence.bkp.$(date +%Y%m%d-%H.%M.%S)
```

