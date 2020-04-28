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
Starting docker-network-tutorial-p2_server_1 ... done
Attaching to docker-network-tutorial-p2_server_1
server_1  | [Mon Apr 13 23:24:15 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
...
```

- Desde otra terminal podemos realizar un netcat para ver lo que nos brinda
```bash
nc localhost 4444
```
Dando como resultado algo similar a
```bash
Bienvenido al servidor de fecha y hora Servidor 1
Mon Apr 13 23:25:41 GMT 2020
```
Mientras que el servicio en la terminal anterior (servidor) nos muestra algo similar a:
```bash
server_1  | [Mon Apr 13 23:25:41 GMT 2020] INFO Cliente conectado /172.22.0.1:59434
```
- Corroborar que también se almacenan los datos del log en el volumen definido
```bash
cat /tmp/javadir/logfile1.log
[Mon Apr 13 23:24:15 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
[Mon Apr 13 23:25:41 GMT 2020] INFO Cliente conectado /172.22.0.1:59434
[Mon Apr 13 23:54:09 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
[Tue Apr 14 00:14:31 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
```

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
Starting docker-network-tutorial-p2_server2_1 ... done
Starting docker-network-tutorial-p2_server3_1 ... done
Starting docker-network-tutorial-p2_server_1  ... done
Attaching to docker-network-tutorial-p2_server_1, docker-network-tutorial-p2_server2_1, docker-network-tutorial-p2_server3_1
server_1   | [Mon Apr 13 23:54:09 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
server2_1  | [Mon Apr 13 23:54:10 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
server3_1  | [Mon Apr 13 23:54:10 GMT 2020] INFO Servidor 3 iniciado en puerto 4444
```

- Revisar como están los contenedores y puertos (prestar atención a los bind de los puertos)
```bash
$ docker container ps
CONTAINER ID        IMAGE                                COMMAND             CREATED             STATUS              PORTS                    NAMES
90bff8a1b185        docker-network-tutorial-p2_server3   "java Servidor"     22 hours ago        Up 2 seconds        0.0.0.0:4442->4444/tcp   docker-network-tutorial-p2_server3_1
bad75e5d8dac        docker-network-tutorial-p2_server2   "java Servidor"     22 hours ago        Up 2 seconds        0.0.0.0:4443->4444/tcp   docker-network-tutorial-p2_server2_1
2609e17074ce        docker-network-tutorial-p2_server    "java Servidor"     22 hours ago        Up 2 seconds        0.0.0.0:4444->4444/tcp   docker-network-tutorial-p2_server_1
```

- Probar con netcat el resultado de los servidores
```bash
$ netcat localhost 4443
Bienvenido al servidor de fecha y hora Servidor 2
Tue Apr 14 00:16:47 GMT 2020
```

- Corroborar que también se almacenan los datos del log en el volumen definido
```bash
$ cat /tmp/javadir/logfile2.log
[Mon Apr 13 23:54:10 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
[Tue Apr 14 00:14:31 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
[Tue Apr 14 00:16:47 GMT 2020] INFO Cliente conectado /172.22.0.1:53830
```

## Sección 4 -- Balancear carga entre instancias de servicios con HaProxy (Desde el HOST)
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
```bash
Active: active (running) since Mon 2020-04-13 22:07:10 -03; 1s ago
```
- Quinto, iniciar el sitio de administración (http://localhost:8181) y validar que estén los nodos en verde funcionando.

- Sexto, realizar pruebas de peticiones y validar que la carga se va repartiendo entre los nodos (a la configuración del bind del haproxy)

```bash
$ telnet localhost 8080 & telnet localhost 8080 & telnet localhost 8080 & telnet localhost 8080
```
- Finalmente, luego de validar las funciones, parar los servicios.
```bash
$ docker container ps 

$ docker container rm ID ID2 ID3
```

## Sección 5 -- Manejo de redes en Docker 

### Sección 5.1 -- Redes básicas Docker
La mayoría de aplicaciones estos día no corren de forma aislada y necesitan comunicarse con otros sistemas a través de la red. Si quisiéramos que un sitio web, un webservice, una base de datos o un servidor de caché en un contenedor de docker entonces necesitamos entender al menos los conceptos básicos de configuración de redes en contenedores docker.
Cuando el proceso de Docker se crea, configura una nueva interfaz puente virtual llamada docker0 en el sistema host. Esta interface permite a Docker crear una sub-red virtual para el uso de los contenedores que se ejecutarán. Este puente funciona como el punto o interfaz principal entre la creación de redes en el contenedor y el host.
Esa red tiene un rango de 172.17.0.xx, el propio servidor docker es 172.17.0.1 y cada contenedor corriendo adquiere un IP posterior al último (172.17.0.2-254).
Docker automáticamente configura las reglas en iptables que permitirán redirigir y condigurar la máscara NAT para el tráfico originado en la interfaz docker0 hacia el resto del mundo.

Además de la red "docker0" en modo bridge, existen 2 redes más preconfiguradas en Docker,

* Bridge. La red standard que usarán todos los contenedores (172.17.0.0/16)
* Host. El contenedor usará el mismo IP del servidor real que tengamos (solo podremos tener un docker container corriendo y mapeado a nuestro "localhost")
* None. Se utiliza para indicar que un contenedor no tiene asignada una red.

### Sección 5.2 -- ¿Cómo los contenedores exponen servicios a los consumidores?
- Comunicación interna:
Si toda nuestra aplicación y servicios corren dentro del mundo "docker" y no tienen vinculación con el mundo exterior, toda la comunicación de los contenedores que están corriendo dentro de la docker0 network (o la que corresponda) pueden comunicarse sin configuraciones adicionales (Lo validaremmos más adelante). Para ello, el sistema host simplemente enruta las peticiones originadas por y destinadas a la interfaz docker0 a la ubicación de los servicios adecuados.
Cuando creamos/añadimos un contenedor a una red, por defecto todos los puertos estarán cerrados en la conexión hacia al exterior y todos abiertos para las máquinas que se encuentren dentro de la misma red. Esto significa que por ejemplo no haría falta exponer puertos del contenedor "mysql", ya que si nos queremos conectar a él desde otro contenedor en la misma red podrán conectarse por el puerto sin problema.  Sin embargo, con esta configuración, no podremos acceder al puerto de mysql desde fuera de la red.

- Comunicación "con el mundo":
Por el contrario, si yo necesito que al menos un servicio tenga contacto con el mundo exterior (por ejemplo otro equipo en la red), voy a tener que tomar acciones para que esto pueda realizarse correctamente.  Más allá que esto ya lo hicimos en apartados anteriores, ahora vamos a entender detalladamente el porque.  
Para lograr este "binding" entre la red interna de docker y la NIC del host es necesario EXPONER los puertos de escucha de los servicios al host (donde es que estos reciben el tráfico redirigido hacia el mundo exterior). Los puertos expuestos pueden ser mapeados al sistema host, tan solo con seleccionar un puerto específico o dejando a Docker seleccionar un puerto al azar, alto y sin usar (no recomendable si queremos una administración de los servicios). Recordar que en este caso no vamos a poder mapear nuestros servicios más de un puerto de contenedor a un puerto de HOST (cosa que ya vimos también)
Para este servicio, Docker se encarga de todas la configuración en las reglas de redirección e iptables en estas situaciones.

* Para el primer caso, no es necesario que hagamos ningún ajuste.
* Para el segundo caso es necesario que hagamos el bind en el docker-file del compose o en el mismo docker run (cómo lo vinimos haciendo en la primer parte de este tutorial), Solo a modo de recordarlo:

```yaml
version: '3'
services:
  server:
    ...
    ports:
      - "4444:4444"
    ...
  server2:
    ...
    ports:
      - "4443:4444"
    ....
```

### Sección 5.3 -- Comandos básicos en Docker Networks

A continuación se presentan los comandos más importantes para gestionar las redes en Docker 

* connect: Permite conectar un contenedor a un red previamente disponible
* disconnect: hace la operación opuesta a la anterior, es decir, desconecta un contenedor de una red
* create es el comando que debes utilizar para crear una red
* inspect te permite obtener información detallada de una red
* ls es el comando que se utiliza para ver todas las redes que hay disponible 
* prune es el comando con el que se borran todas las redes creadas (excepto las redes por defecto -bridge, host, none-) 
* rm permite borrar una o mas redes especificando ciertos argumentos 

### Sección 5.4 -- Comenzando con las "Dockers networks" 
Primero analizaremos las redes que están disponibles para el usuario una vez que Docker es instalado

```bash

$ docker network ls
NETWORK ID          NAME                DRIVER
7fca4eb8c647        bridge              bridge
9f904ee27bf5        none                null
cf03ee007fb4        host                host
```
Luego, podemos ver la configuración detallada de una determinada red a través del siguiente comando (se muestran los mas relevantes)

```bash
$ docker network inspect bridge
[
    {
        "Name": "docker-new-bridge",
        "Driver": "bridge",
        .....
        "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        .....
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        ......
    
```
Siendo el nombre del adaptador de red asociado "docker0" que podemos ver en nuestro comando ifconfig

```bash
$ ifconfig 
....
docker0: flags=4099<UP,BROADCAST,MULTICAST>  mtu 1500
inet 172.17.0.1  netmask 255.255.0.0  broadcast 172.17.255.255
....

```
Cómo puede verse, esta información se condice con la información detallada anteriormente

### 5.5 -- Entendiendo que pasa cuando creamos contenedores en la red "bridge" por defecto 

Vamos a volver a correr el servidor java que desarrollamos para este proyecto (o podría ser cualquier otra imagen) y la ponemos a correr

- Para forzar a los que los contenedores corran en la red por defecto "bridge", ajustamos el yaml del docker-compose
Archivo: docker-compose-bridge-java.yml

```yaml
version: '3'
services:
  server1:
    build: ./servidor
    network_mode: bridge
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
    network_mode: bridge
    ports:
      - "4443:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
      - nombre=Servidor 2
      - logName=logfile2
      - port=4444
```
- Una vez ajustado el yaml, levantamos nos dos nodos java a través del comando habitual de docker compose

```bash
$ docker-compose -f docker-compose-bridge-java.yml up
...
Attaching to docker-network-tutorial-p2_server1_1, docker-network-tutorial-p2_server2_1
server1_1  | [Mon Apr 20 00:33:17 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
server2_1  | [Mon Apr 20 00:33:17 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
...
```

- Revisamos que los dos nodos estén corriendo

```bash
$ docker container ps
CONTAINER ID        IMAGE                                COMMAND             CREATED             STATUS              PORTS                    NAMES
f2e300f725d6        docker-network-tutorial-p2_server1   "java Servidor"     5 minutes ago       Up 2 seconds        0.0.0.0:4444->4444/tcp   docker-network-tutorial-p2_server1_1
965ca0ae921c        docker-network-tutorial-p2_server2   "java Servidor"     5 minutes ago       Up 2 seconds        0.0.0.0:4443->4444/tcp   docker-network-tutorial-p2_server2_1
```

- Ahora vamos a revisar que configuración de IP "interna" tiene cada nodo "java" corriendo.  Hay dos opciones para hacer esto
a) con docker container inspect ID, y buscar la información de ipv4
b) con docker network inspect bridge, y ver los datos de los contenedores.  Vamos por esta última opción
```bash
$ docker network inspect bridge
...
"Containers": {
            "965ca0ae921cf013666589545ed7a6600683daf9fa9e5507493a55e3a0ee5cad": {
                "Name": "docker-network-tutorial-p2_server2_1",
                "EndpointID": "931dbace497085cc7487146e2f9a7fd9017c87e9e6baee5f0d05f9974ec4378f",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            },
            "f2e300f725d62cd1356fc464f02545ced4e7b18e657fb681233a9ea36b2dd268": {
                "Name": "docker-network-tutorial-p2_server1_1",
                "EndpointID": "17d13fb5cac4fde4eec4502f0544df0adc498d0655b34913f182e9bb19b0ea73",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            }
        },

...
```
- Pudimos obtener, entonces, las dos direcciones IP de nuestros nodos java (172.17.0.2 y 172.17.0.3).  Ahora vamos a validar que desde nuestro host podemos "llegar" a dichas direcciones.  Esto es posible ya que la red fue creada en modo puente de nuestra NIC.  
Para ello la opción más simple es el telnet o netcat a IP:PORT (a los puertos originales de la aplicación, no de los bind que hicimos en el docker-file ya que no estamos accediendo desde el bind, sino de la ip local de la red bridge)

```bash
$ nc 172.17.0.2 4444
Bienvenido al servidor de fecha y hora Servidor 1
Mon Apr 20 00:45:41 GMT 2020

$ nc 172.17.0.3 4444
Bienvenido al servidor de fecha y hora Servidor 2
Mon Apr 20 00:46:06 GMT 2020

```
De esta manera pudimos validar que desde el host es posible también acceder a los servicios si conocemos la IP que otorga el DNS del bridge (problema a analizar un poco más adelante)

### 5.6 -- Accediendo por SSH a los contenedores y verificando conectividad 
- Aprovechando que tenemos los dos nodos levantados .0.2 y 0.3, vamos a ver:
a) Cómo conectarse por SSH a un contenedor
b) Validar que los contenedores en la docker bridge se ven

Esto es importante para comprender que las aplicaciones que desarrollemos se pueden comunicar independientemente de lo que suceda en el "mundo del HOST" mientras la red definida (default / propia) esté activa y obviamente que el host esté activo.  Para ello tomamos algunos de los IDS de los contenedores y luego nos conectamos a dicho nodo.

```bash
$ docker container ps
CONTAINER ID        IMAGE                                COMMAND             CREATED             STATUS              PORTS                    NAMES
f2e300f725d6        docker-network-tutorial-p2_server1   "java Servidor"     5 minutes ago       Up 2 seconds        0.0.0.0:4444->4444/tcp   docker-network-tutorial-p2_server1_1
965ca0ae921c        docker-network-tutorial-p2_server2   "java Servidor"     5 minutes ago       Up 2 seconds        0.0.0.0:4443->4444/tcp   docker-network-tutorial-p2_server2_1

$ docker exec -it f2e300f725d6 /bin/bash
bash-4.2# yum update -y ; yum install net-tools -y ; yum install telnet -y

bash-4.2# ifconfig
eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.17.0.3  netmask 255.255.0.0  broadcast 172.17.255.255
....

bash-4.2# telnet 172.17.0.2 4444
Trying 172.17.0.2...
Connected to 172.17.0.2.
Escape character is '^]'.
...
```

### 5.7 -- Limitantes de una red Docker por defecto
La configuración de red por defecto de Docker es funcional, pero bastante simple.  Recordar que por definición las redes en docker son privadas y seguras, es decir, un contenedor conectado a una red no puede ver los contenedores conectados a otra red diferente.  Esto puede no ser adecuado para algunos proyectos que pueden requerir características específicas y mayor flexibilidad.
Entonces, además de las redes que vienen por defecto (bridge, host, none) existe la posibilidad de crear redes personalizadas según las necesidades del usuario o de las aplicaciones.  Para ello vamos a crear redes privadas diferentes sobre las cuales conectaremos los distintos contenedores que tengamos.

Si bien no vamos a utilizar configuraciones avanzadas, vamos a intentar asignar una dirección IP específica a un contenedor (dentro del rango permitido) y ver que nos dice la docker bridge network. Para verificar rápidamente y no modificar nuestros archivos yamls ya definidos vamos a utilizar una imagen de NGINX server disponible en Docker Hub. 

* Primero probamos la imagen básica:

```bash
#---------------network      --container name  container image
$ docker run -d --net bridge --name nginx-test nginx

$ docker container ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
8fd2d1e72456        nginx               "nginx -g 'daemon of…"   13 seconds ago      Up 11 seconds       80/tcp              nginx-test
```
* Vemos que levantó correctamente y vemos en su configuración que tiene asignada una dirección IP
```bash
$ docker container inspect 8fd2d1e72456 | grep IPAddress
"SecondaryIPAddresses": null,
            "IPAddress": "172.17.0.2",
                    "IPAddress": "172.17.0.2",

```
* Si ponemos eso en nuestro navegador (porque estamos en el mismo HOST) podemos acceder y veremos algo del estilo:
>Welcome to nginx!
>If you see this page, the nginx web server is successfully installed and...

* Ahora paramos el servidor nginx.
```bash
$ docker container rm 8fd2d1e72456 -f
```

* Ahora vamos a intentar setearle una IP específica al servidor NGINX (que sabemos que no está permitido en una red por defecto)
```bash
$ docker run -d --net bridge --ip 172.17.0.16 --name nginx-network nginx
docker: Error response from daemon: user specified IP address is supported on user defined networks only.

```

De esta manera pudimos verificar que no es posible tomar esa acción en una red definida por defecto.

### 5.8 -- Crear una red de Docker personalizada
Cómo sucedió a lo largo de todo el tutorial de docker, existen dos posibilidades para crear una red Docker:
a) A través del comando docker network create
b) A través de la definición de los servicios en docker-compose.

* A modo de ejemplo, vamos a comenzar con la consola de docker. 
```bash
$ docker network create --driver=bridge --subnet=172.30.0.0/16 --gateway=172.30.0.1 network-bridge-from-console
```
* Vamos a validar que la red se haya construido correctamente y analizaremos su configuración básica, para ello:
```bash
$ docker network ls 
...
3ab4e4794cd3        network-bridge-from-console          bridge              local
...
$ docker network inspect network-bridge-from-console 
...
 "Driver": "bridge",
...
...
            "Config": [
                {
                    "Subnet": "172.30.0.0/16",
                    "Gateway": "172.30.0.1"
                }
            ]
        },
...
```
* A continuación, vamos a levantar nuevamente el servicio de NGINX pero ahora en la red creada por nosotros y le vamos a asignar una dirección de IP disponible en el rango de la misma.

```bash
$ docker run -d --net network-bridge-from-console --ip 172.30.0.16 --name nginx-network nginx

```
* Ahora vamos a validar que dirección IP tiene 

```bash
$ docker container ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS               NAMES
591a212f4baf        nginx               "nginx -g 'daemon of…"   5 seconds ago       Up 4 seconds        80/tcp              nginx-network

$ docker container inspect 591a212f4baf | grep IPAddress
"SecondaryIPAddresses": null,
            "IPAddress": "",
                    "IPAddress": "172.30.0.16",

```
* Una vez obtenida la IP (que ya la sabíamos), vamos a entrar al navegador y validar que podemos llegar desde nuestro HOST. Deberíamos ver algo similar a:
>Welcome to nginx!
>If you see this page, the nginx web server is successfully installed and...

* A continuación vamos a integrar esto que previamente vimos a través de la consola de docker en el formato de docker-compose para adptar nuestras aplicaciones en docker compose 
Archivo: redes_docker/docker-compose-my-network.yml
```yaml
version: '3'
services:
  server1:
    build: ../servidor
    ports:
      - "4444:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
      - nombre=Servidor 1
      - logName=logfile1
      - port=4444
    #DEFINO QUE RED VOY A UTILIZAR (ABAJO DEBO ESPECIFICAR SI ES NUEVA O EXISTENTE)
    networks:
      default:
        # DEFINIMOS UNA IP ESTÁTICA
        ipv4_address: 172.30.0.10
  server2:
    build: ../servidor
    ports:
      - "4443:4444"
    volumes:
      - /tmp/javadir:/tmp/javadir
    environment:
      - nombre=Servidor 2
      - logName=logfile2
      - port=4444
    networks:
      default:
        # DEFINIMOS UNA IP ESTÁTICA
        ipv4_address: 172.30.0.11
#VAMOS A MAPEAR EL NOMBRE DE LA RED CON LA RED REAL (PREEXISTENTE EN ESTE CASO)        
networks:
  default:
    #COMO ERA EXISTENTE, LE DECIMOS QUE ES "EXTERNAL"
    external:
      name: network-bridge-from-console
```
* Una vez definido, poner a correr los servicios.

```bash
$ docker-compose -f docker-compose-my-network.yml up
Starting redes_docker_server2_1 ... done
Starting redes_docker_server1_1 ... done
Attaching to redes_docker_server1_1, redes_docker_server2_1
server1_1  | [Mon Apr 20 21:27:20 GMT 2020] INFO Servidor 1 iniciado en puerto 4444
server2_1  | [Mon Apr 20 21:27:20 GMT 2020] INFO Servidor 2 iniciado en puerto 4444
```
* En otra terminal, verificar que en la red mencionada (network-bridge-from-console) están las dos IPS mencionadas en uso 
```bash
$ docker network inspect network-bridge-from-console | grep IPv4
"IPv4Address": "172.30.0.11/16",
"IPv4Address": "172.30.0.16/16",
"IPv4Address": "172.30.0.10/16",

```
## Sección 6 -- Crear un cluster RabbitMQ con Docker Compose

### 6.1 - Qué es RabbitMQ?

> RabbitMQ es un "open source message broker software" que implementa los protocolos de "Advanced Message Queuing Protocol (AMQP)".
> El servicio de RabbitMQ está escrito en Erlang y construido por Open Telecom Platform
> Tiene la capacidad de construir su servicio como un cluster con capacidad de redundancia ante fallas. Están disponibles  
> API's para varios lenguajes de programación

https://www.rabbitmq.com/

### 6.2 - RabbitMQ en Docker
La imagen oficial de RabbitMQ se encuentra desarrollada y presentada (desde Marzo de 2020) por Bitnami Inc y se encuentra preparada para trabajar en diversos entornos (local, Cloud, Kubernetes, etc)
https://bitnami.com/stack/rabbitmq/containers

La imagen base se encuentra en Docker Hub, donde se puede visualizar el contínuo update de la misma. 
https://hub.docker.com/r/bitnami/rabbitmq/

### 6.3 - Porqué usar las imágenes Bitnami

* Bitnami monitorea contínuamente los cambios que producen los proveedores de las plataformas y publica rápidamente las nuveras versiones de esas imágenes a través de pipelines automatizados 
* Con las imágenes de Bitnami, las últimas correcciones de errores y características están disponibles lo antes posible.
* Los contenedores de Bitnami, las máquinas virtuales y las imágenes en la nube utilizan los mismos componentes y el mismo enfoque de configuración, lo que facilita el cambio entre formatos según las necesidades de su proyecto.
* Todas las imágenes de bitnami están basadas en [minideb] (https://github.com/bitnami/minideb) que es una imagen de contenedor minimalista de tamaño reducido basada en Debian (distribución líder de Linux) .
* Todas las imágenes de Bitnami disponibles en Docker Hub están firmadas con [Docker Content Trust (DTC)] (https://docs.docker.com/engine/security/trust/content_trust/). Puede usar `DOCKER_CONTENT_TRUST = 1` para verificar la integridad de las imágenes

Cómo siempre existen varias maneras de crear un cluster de un determinado servicio para ofrecer características de redundancia, tolerancia a fallos y replicación. Especialmente en docker existen siempre diversos usuarios que publican adaptaciones de las imágenes oficiales para funciones específicas o facilitar el uso de algún plugin o herramienta.  Sin embargo, este tipo de adaptaciones casi siempre quedan sin actualizaciones frecuentes y terminan ofreciendo herramientas con ciertas deficiencias.  Por lo tanto, en este tutorial siempre se busca utilizar herramientas de repositorios oficiales con updates contínuos.

### 6.4 - Cómo correr RabbitMQ desde la imagen básica (docker) de Bitnami

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

#### 6.5 - Usar Docker Compose para correr RabbitMQ 

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

#### 6.6 - Persistir los datos de RabbitMQ (a pesar de reinicio) 

Si se elimina el contenedor, todos sus datos se perderán, y la próxima vez que ejecute la imagen, la "base de datos" se reiniciará. Para evitar esta pérdida de datos, debe montar un volumen que persistirá incluso después de quitar el contenedor.

Para lograr aplicar persistencia, se debe montar un directorio en la ruta `/bitnami`. Si el directorio montado está vacío, se inicializará en la primera ejecución.

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

#### 6.7 - Configuración avanzada de RabbitMQ (Docker)

##### Configurar RabbitMQ a través de variables de entorno

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

#### 6.8 - Construir un cluster RabbitMQ usando Docker Compose

A continuación se definen los pasos a seguir para construir un cluster en RabbitMQ:

##### Paso 1: Crear en /tmp/ los "volúmenes" que usaremos para montar en los nodos 
Vamos a crear:
* 1 carpeta (storage) para el nodo de "stats-administración" 
* 1 carpeta para cada nodo "cola-queue-disc"

```bash
$ mkdir /tmp/rabbit; mkdir /tmp/rabbit/stats ; mkdir /tmp/rabbit/node1 ; mkdir /tmp/rabbit/node2 ; mkdir /tmp/rabbit/node3; sudo chmod 777 -R /tmp/rabbit
```
##### Paso 2: Crear el primer nodo stats en Docker compose 
Archiv: docker-compose.yml

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
#### 6.9 - Ajustar el módulo de HAProxy para balancear carga 
* Una vez instalado se va a proceder a configurar el balanceador en base a la arquitectura definida.  Como archivo de configuración en sistema Linux se encuentra dentro del directorio etc.  En este caso en el archivo se encuentra en /etc/haproxy/haproxy.cfg

- Primero, realizar un bkp del archivo original
```bash
$ cp /etc/haproxy/haproxy.cfg /etc/haproxy/haproxy_old.cfg
```
- Agregar al final del archivo, lo siguiente

```bash

listen rabbitmq_service5672
	#todo lo que venga a 5672
bind 0.0.0.0:5672
mode tcp
	#Lo balanceo con mismo peso (RoundRobin)
balance roundrobin
	#Evitar desconexiones de los CLI se configura timeout client/server 3h
timeout client 3h
timeout server 3h
	#Configuracion clitcpka -> enviarse paquetes de Heartbeat (Cliente) y no se pierda conexion
option clitcpka

server rabbit1 0.0.0.0:5672 check inter 5s rise 2 fall 3
server rabbit2 0.0.0.0:5673 check inter 5s rise 2 fall 3
server rabbit3 0.0.0.0:5674 check inter 5s rise 2 fall 3

listen rabbitmq_service4369
	#todo lo que venga a 4369
bind 0.0.0.0:4369
mode tcp
	#Lo balanceo con mismo peso (RoundRobin)
balance roundrobin
	#Evitar desconexiones de los CLI se configura timeout client/server 3h
timeout client 3h
timeout server 3h
	#Configuracion clitcpka -> enviarse paquetes de Heartbeat (Cliente) y no se pierda conexion
option clitcpka

server rabbit1 0.0.0.0:4369 check inter 5s rise 2 fall 3
server rabbit2 0.0.0.0:4370 check inter 5s rise 2 fall 3
server rabbit3 0.0.0.0:4371 check inter 5s rise 2 fall 3


listen rabbitmq_management
	#todo lo que venga a 15672
    bind 0.0.0.0:15672
    mode tcp
	#Lo balanceo con mismo peso (RoundRobin)
    balance roundrobin
    server stats 0.0.0.0:15672 check fall 3 rise 2
    

listen stats
	#Interfaz de administracion
    bind 0.0.0.0:8181
    stats enable
    stats uri /
    stats realm Haproxy\ Statistics
    stats auth admin:admin
    stats refresh 5s
```
### FALTA HACER LAS PRUEBAS DE CAIDA DE LOS NODOS DE RABBITMQ 

## Sección 7 -- "Armar" un cluster de RabbitMQ + Cluster de HAProxy + KeepAlived
Para construir un cluster se requeriría tener una red con la que se puedan obtener más de una IP y no solo la local (Por ejemplo con Docker Swarm o Kubernetes).  Para solventar este apartado vamos a intentar crear un balanceador en cluster redundante pero dentro de la docker network que creamos previamente y lo haremos redundante y tolerante a fallos dentro de dicha red. Este apartado hará un repaso intenso de todos los conceptos visto en el curso por lo que se requiere especial atención en cada uno de los puntos.  El consumo del cluster de RabbitMQ y de los webservers Java será a través de clientes dentro de la docker network. 

### 7.1 - Cluster RabbitMQ 
Para construir el cluster de rabbitMQ vamos a setearle (en base a lo que hicimos en todos los pasos anteriores) IP fija.
Archivo cluster_rabbitmq/docker-compose-cluster-rabbitmq.yml

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
      - 15672:15672
      - 5672:5672
      - 4369:4369
    networks:
      default:
        ipv4_address: 172.30.0.30
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
      - 5673:5672
      - 4370:4369
    networks:
      default:
        ipv4_address: 172.30.0.31
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
      - 5674:5672
      - 4371:4369
    networks:
      default:
        ipv4_address: 172.30.0.32
    volumes:
      - '/tmp/rabbit/node2:/bitnami'   
networks:
  default:
    external:
      name: network-bridge-from-console
```


### 7.2 -- Crear la imagen docker de haproxy adaptada a nuestra necesidad (configuración de haproxy.cfg)
* Definir el archivo haproxy.cfg
Archivo: imagenpersonalizada/haproxy.cfg

```bash
global
    maxconn 4096
    daemon

defaults
    log     global
    mode    http
    
    timeout connect 5000
    timeout client  50000
    timeout server  50000
    
listen rabbitmq_service5672
	#todo lo que venga a 5672
	bind 0.0.0.0:5672
	mode tcp
		#Lo balanceo con mismo peso (RoundRobin)
	balance roundrobin
		#Evitar desconexiones de los CLI se configura timeout client/server 3h
	timeout client 3h
	timeout server 3h
		#Configuracion clitcpka -> enviarse paquetes de Heartbeat (Cliente) y no se pierda conexion
	option clitcpka

	server stats 172.30.0.30:5672 check inter 5s rise 2 fall 3
	server rabbit1 172.30.0.31:5672 check inter 5s rise 2 fall 3
	server rabbit2 172.30.0.32:5672 check inter 5s rise 2 fall 3

listen rabbitmq_service4369
	#todo lo que venga a 5672
	bind 0.0.0.0:4369
	mode tcp
		#Lo balanceo con mismo peso (RoundRobin)
	balance roundrobin
		#Evitar desconexiones de los CLI se configura timeout client/server 3h
	timeout client 3h
	timeout server 3h
		#Configuracion clitcpka -> enviarse paquetes de Heartbeat (Cliente) y no se pierda conexion
	option clitcpka

	server stats 172.30.0.30:4369 check inter 5s rise 2 fall 3
	server rabbit1 172.30.0.31:4369 check inter 5s rise 2 fall 3
	server rabbit2 172.30.0.32:4369 check inter 5s rise 2 fall 3

listen rabbitmq_management
	#todo lo que venga a 15672
    bind 0.0.0.0:15672
    mode http
	#Lo balanceo con mismo peso (RoundRobin)
    balance roundrobin
    server stats 172.30.0.30:15672 check fall 3 rise 2

listen stats
	#Interfaz de administracion
    bind 0.0.0.0:8181
    stats enable
    stats uri /
    stats realm Haproxy\ Statistics
    stats auth admin:admin
    stats refresh 5s
```
* Definimos el archivo Dockerfile para crear la imagen personalizada
```yaml
FROM haproxy:latest
COPY haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg
```
* Construimos la imagen 
```bash
$ docker build . -t haproxy-personalized
```

* ahora definimos nuestro yaml del docker-compose para el servicio haproxy
Arcjivo: docker-compose-haproxy.yml
```yaml
version: '3'
services:
  haproxy1:
    image: haproxy-personalized
    networks:
      default:
        ipv4_address: 172.30.0.40
networks:
  default:
    external:
      name: network-bridge-from-console
```

* Corremos el docker-compose
$ docker-compose -f docker-compose-haproxy.yml up
la imagen adaptada ALTA HACER LAS PRUEBAS DE CAIDA DE LOS NODOS DE RABBITMQ 


















Luego de instalar hay que configurar el producto (con diferentes configuraciones en los equipos debido a uno se configurará como Maestro y otro como Esclavo.


interface: donde indicamos la tarjeta de red de nuestro servidor.
state: le decimos cual va a ser el MASTER y cual el BACKUP.
priority: daremos más prioridad al servidor maestro de tal forma que en caso de que los dos servidores HAProxy estén iniciados tomará toda la carga de conexiones.
virtual_router_id: identificador numérico que tiene que ser igual en los dos servidores.
auth_pass: especifica la contraseña utilizada para autenticar los servidores en la sincronización de failover.
virtual_ipaddress: será la dirección IP virtual que compartirán lo dos servidores y a la que tienen que realizar las peticiones los clientes.

```
docker run -d --net network-bridge-from-console --ip 172.30.0.40 --name haproxy1 haproxy-david































```bash
$ cat /etc/network/interfaces

# The loopback network interface
auto lo
iface lo inet loopback

```
- Ahora vamos a crear una interfaz vinculada a nuestra placa de red (wifi en mi caso).  Para averiguar cual es la activa ejecutamos:

```bash
$ ifconfig 
....

wlp5s0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
inet 192.168.0.25  netmask 255.255.255.0  broadcast 192.168.0.255
.....
También lo pueden hacer con ip addr list

```
- Sabiendo el nombre del adaptador, ahora vamos a crear el adaptador virtual relacionado con esa placa

```bash
auto wlp5s0:0
iface wlp5s0:0 inet static
        address 192.168.0.101
        netmask 255.255.255.0
```
- Guardar cambios y reiniciar configuración de la placa

```bash
$ systemctl restart networking
```
- Verificar que disponemos de la placa virtual asociada a nuestra NIC
```bash
$ ifconfig 
....
wlp1s0:0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 192.168.0.110  netmask 255.255.255.0  broadcast 192.168.0.255
        ether 40:f0:2f:fd:e6:ac  txqueuelen 1000  (Ethernet)
....

```

* Luego de instalar KeepAlived hay que configurar el producto (con diferentes configuraciones en los equipos debido a uno se configurará como Maestro y otro como Esclavo)

* interface: donde indicamos la tarjeta de red de nuestro servidor.
* state: le decimos cual va a ser el MASTER y cual el BACKUP.
* priority: daremos más prioridad al servidor maestro de tal forma que en caso de que los dos servidores HAProxy estén iniciados tomará toda la carga de conexiones.
* virtual_router_id: identificador numérico que tiene que ser igual en los dos servidores.
* auth_pass: especifica la contraseña utilizada para autenticar los servidores en la sincronización de failover.
* virtual_ipaddress: será la dirección IP virtual que compartirán lo dos servidores y a la que tienen que realizar las peticiones los clientes.

