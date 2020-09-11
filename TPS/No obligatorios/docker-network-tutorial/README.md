Bienvenidos al tutorial Docker network - Parte 1. 
Este tutorial te guiará en:
- Comprender los aspectos básicos de Docker
- Utilizar y crear tus propias imágenes
- Configurar contenedores utilizando Dockerfiles
- Crear volúmenes para persistir y actualizar cambios
- Crear una red con Docker compose, conectando dos programas en contenedores distintos

# Sección 1 -- Obtener el entorno
El primer requisito para crear un contenedor es buscar un entorno que pueda correr la aplicación a desarrollar. Afortunadamente, la comunidad de Docker tiene un repositorio de imágenes actualizado con muchos recursos ya empaquetados y listos para usar. En este caso, utilizaremos la imagen de openjdk que ya viene lista para correr aplicaciones Java (https://hub.docker.com/_/openjdk). Para eso, se debe descargar la imagen con el comando:
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

CONTAINER ID        IMAGE                         COMMAND                  CREATED             STATUS                      PORTS               NAMES
94beef1ca623        seqvence/static-site:latest   "/bin/sh -c 'cd /usr…"   25 hours ago        Exited (137) 22 hours ago                       static-site
e7cfb37bac35        wordpress:latest              "docker-entrypoint.s…"   26 hours ago        Exited (0) 25 hours ago                         simple-wordpress_web_1
6bf5ca243c99        mariadb:latest                "docker-entrypoint.s…"   26 hours ago        Exited (0) 25 hours ago                         simple-wordpress_db_1
.....
```
Notar que sin *--all* sólo se verían los contenedores corriendo actualmente.

Si se quiere detener todos los contenedores se puede utilizar el siguiente comando combinado
```bash
$ docker stop $(docker ps -a -q)
```
Esto significa: 
Primero ejecutar lo que está en paréntesis, teniendo en cuenta que
-a		Significa mostrar todos los contenedores (no solo los que están corriendo)
-q		Significa solo mostrar el ID Numérico del Contenedor
```bash
$ docker ps -a -q
2b69cf17a259
....
```
Y luego ejecutar el comando externo, usando los resultados del primero. (En este caso, hacer un docker stop de los IDs de contenedores antes obtenidos)

Si también se quiere eliminar todos los contenedores se puede utilizar el siguiente comando combinado
```bash
$ docker rm $(docker ps -a -q)
```
Obviamente, se pueden utilizar los dos comandos al mismo tiempo, a través de un "concatenador" en bash (puede usarse ";" o "&&")
```bash
$ docker stop $(docker ps -a -q) ; docker rm $(docker ps -a -q)
```

# Sección 2 -- Creando nuestra propia imagen con Dockerfile
Ahora es posible usar la imagen descargada (openjdk) para tomarla como base y crear nuestra "propia imagen". En nuestro caso contendrá la aplicación de ejemplo a ejecutar (Servidor). Para esto, en el repositorio está incluida la estructura y contenido necesario:
```
servidor
    |- Dockerfile
    |- src
        |-- Servidor.java
```
Nota de color: En esta parte del tutorial se va a trabajar desde el directorio *servidor*.<br/>
Contenido: 
* Servidor.java es una aplicación simple que se pone en escucha en el puerto que indiquemos por argumento, y contesta a quienes se conectan con la hora del servidor.
```java

    public static void main(String[] args) {
    	// [STEP 1] - Recibir el puerto por parámetro
    	int port = Integer.parseInt(args[0]);
        // [STEP 2] - Crear un "Servidor Socket" (Podría ser un servidor web o cualquier otra cosa) 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // [STEP 3] - Llamar a la clase LOG y pasarle la información (puerto)
            Log("Servidor iniciado en puerto " + String.valueOf(port));
            // [STEP 4] - Crear un loop "para siempre"
            while (true) {
            	// Aceptar conexiones de clientes
                try {
                    // [STEP 5] - Aceptar un cliente 
                	Socket clientSocket = serverSocket.accept();
                    // [STEP 6] - Mandar a la clase LOG la información del cliente
                    Log("Cliente conectado " + clientSocket.getRemoteSocketAddress());
                    // [STEP 7] - Abrir el canal de salida para poder escribirle la respuesta
                	PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("Bienvenido al servidor de fecha y hora");
                    out.println(new Date().toString());
                }catch(IOException e) {
                    e.printStackTrace();
                }
            
            
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

```
* Dockerfile: Es la clave para la construcción de la "imagen" de nuestra aplicación. El archivo contiene las siguientes líneas:

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
- WORKDIR: cambiar el directorio de trabajo al especificado (igual al comando *cd* en Linux).
- RUN: comando que se ejecuta cuando se construye (build) por primera vez la imagen. En este caso se busca compilar la clase Servidor.java.
- EXPOSE: indica a Docker qué puerto escucha el contenedor.
- CMD: comando que se debe ejecutar cuando el contenedor *se inicia* (distinto a cuando se construye). En este caso se quiere ejecutar la aplicación java, y se le pasa por parámetro el nombre de la clase compilada y un argumento (el puerto).

Con esta configuración ya es posible construir la imagen de la aplicación, a través del siguiente comando
```bash
$ docker build -t tutorial-red-servidor .
```
El parámetro *-t* permite ponerle un nombre a la imagen, en este caso *tutorial-red-servidor*. El "." al final indica que la imagen se debe construir tomando el archivo por defecto (Dockerfile) que se encuentra en el directorio actual (servidor).
Con esto, se hace la construcción de la imagen. Podemos verificar la creación de la misma y el tiempo de último update con el siguiente comando:
```docker
$ docker image ls
REPOSITORY                    TAG                 IMAGE ID            CREATED             SIZE
wordpress                     latest              e492f6febf4b        5 days ago          546MB
dpetrocelli/dev-restserver2   latest              e14811d92414        5 weeks ago         534MB
dpetrocelli/dev-restserver2   <none>              cb874034535f        5 weeks ago         534MB
.....
```

# Sección 3 -- Creando el contenedor
Una vez creada la imagen, esta se debe instanciar en un contenedor para poder correr la aplicación. Para crear el contenedor se utiliza el comando *docker run*
```bash
$ docker run --name tutorial-red-servidor -p 4444:4444 tutorial-red-servidor:latest
```
donde los parámetros significan:
* *--name* permite definir el nombre del contenedor; en este caso, tutorial-red-servidor
* *-p* le indica a Docker que publique el/los puerto/s del contenedor al host, en formato <hostPort>:<containerPort>. Esto permitirá conectarse al contenedor con la dirección *localhost:4444*
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
La nueva terminal se "conectará" al proceso servidor TCP que está escuchando en el puerto 4444 y responderá con la fecha y hora actual. La terminal que ejecuta el servidor mostrará un log de la conexión.
Para detener el contenedor, presionar CTRL + C.
Finalmente, si se quiere volver a correr el contenedor, ingresar el comando
```bash
$ docker start tutorial-red-servidor -a
```
La opción *-a* de *attach* mostrará la salida estándar y de error en la terminal donde se ejecutó (recordar usar *docker ps --all* para ver todos los contenedores).

# Sección 4 -- Añadiendo persistencia a través de los Volúmenes
Hasta ahora solamente se tiene acceso a la salida estándar (y errores) de la aplicación montada en el contenedor. Sin embargo, la clase Servidor JAVA también genera un archivo de log en el directorio donde se ejecuta la aplicación. ¿Dónde? Si se acuerdan, se definió cuando creamos la imagen Dockerfile que el "WORKDIR" de trabajo iba a ser */usr/src/app*. Entonces, el archivo de log va a quedar en */usr/src/app/info.log*. 
Ahora... Muy lindo el log en el contenedor, pero.... ¿No surgen algunas preguntas como....?
* ¿Cómo se puede acceder a este directorio desde fuera del contenedor? 
* ¿Que pasa con la información cuando un contenedor es borrado o si sufre un reinicio? 
* ¿Cómo puedo compartir un directorio para varios contenedores?
* ¿Cómo veo los archivos desde dentro de un contenedor?.

Todas las preguntas, en principio, se contestan a partir de la misma respuesta, el uso de volúmenes.
Un volumen es un directorio o un fichero en el host (Administrado por el usuario del SO) o un volumen de Docker (Administrado por Docker) que se monta directamente en el contenedor. 
Al usar volúmenes en contenedores que escriben información en disco (en la carpeta compartida correspondiente) se evita que se mezcle el tamaño del contenedor con el tamaño de los datos persistentes compartidos.
Podemos montar varios volúmenes en un contenedor y en varios contenedores podemos montar un mismo volumen.

¿Qué tipos de volúmenes hay entonces?
<p align="left"> <img src="https://www.returngis.net/wp-content/uploads/2019/02/types-of-mounts-volume.png" width="350"/> </p> 

**Existen tres tipos de almacenamiento:**

* volumes: 
    -  Docker almacena los datos dentro de un área que él controla del sistema de ficheros del equipo HOST. 
    -  Es el mecanismo preferido para persistir los datos a día de hoy (según Docker). Los volúmenes se almacenarán en */var/lib/docker/volumes/* y **solo Docker tiene permisos** sobre esta ubicación (solo con un usario ROOT se puede entrar y revisar que tiene).
    -  Un volumen puede ser montado por diferentes contenedores a la vez.
    -  La administración se realiza "completamente" mediante los comandos vía Docker CLI o Docker API.
    -  Hay que definirle un nombre descriptivo, para poder "localizarlo" amigablemente o hacer un backup
    -  Tienen funcionalidades extra que bind mount (el próximo a ver) no tiene. Por ejemplo, drivers que te "permiten" almacenar los volúmenes en sitios remotos o cifrarlos.

* bind mounts: 
    -  Se utiliza para mapear cualquier sitio del sistema de ficheros dentro de tu contenedor. 
    -  A diferencia de los volúmenes, a través de este mecanismo es posible acceder a la ruta mapeada y modificar los ficheros (sin tantos permisos). Por lo tanto los cambios que realice en el HOST serán reflejados en el contenedor/es y viceversa
    -  Históricamente esta era la única opción que existía en las primeras fases de Docker. 
    -  Estamos más limitados al host, a su sistema de ficheros, y a que con volumes puedes utilizar drivers para almacenar en remoto
    
* tmpfs: Se trata de un almacenamiento temporal en memoria. Se suele utilizar para el almacenamiento de configuraciones y espacios efímeros que desparecerán cada vez que el contenedor se pare (No aplica a lo que buscamos)

Ahora vamos a llevarlo a la práctica. Empecemos por crear un volumen con Docker

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
        "CreatedAt": "2020-09-09T18:45:37-03:00",
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/servidor-log/_data",
        "Name": "servidor-log",
        "Options": {},
        "Scope": "local"
    }
]
```
Podemos hacer algunas búsquedas más detalladas que "ls" según los campos anteriores, por ejemplo Name o Driver
```bash
$ docker volume ls -f name=servidor-log
ó 
$ docker volume ls -f driver=local
```
Finalmente, podemos eliminar el volumen con el siguiente comando
```bash
$ docker volume rm servidor-log
```
Y si quisiéramos eliminar todos los volúmenes que gestiona Docker, lo podemos hacer a través del siguiente comando
```bash
$ docker volume prune
WARNING! This will remove all local volumes not used by at least one container.
Are you sure you want to continue? [y/N] 
...
Total reclaimed space: 842.4MB
```
Ahora vamos a volver a correr nuestro contenedor, pero agregando al mismo el volumen previamente definido para mantener persistencia de los datos que deseemos
```bash
$ docker run --name tutorial-red-servidor -v servidor-log:/usr/src/app -p 4444:4444 tutorial-red-servidor:latest
```
En el comando *-v* se especifica **nombre del volumen:ruta en el contenedor**. En este caso, y como está definido en el Dockerfile, se persiste el directorio raíz de la aplicación. Cómo no existía el nombre del volúmen Docker se encargará de crearlo.
Además, en otra pestaña le hacemos algunas peticiones
```bash
$ nc localhost 4444 & nc localhost 4444
```
Si ahora se accede al directorio definido en *Mountpoint*, se podrá leer el archivo de log. En otra terminal o pestaña, accedemos al log con alguna de las siguientes dos opciones
```bash
$sudo tail -f /var/lib/docker/volumes/servidor-log/_data/info.log 
ó
$sudo less +F /var/lib/docker/volumes/servidor-log/_data/info.log
....
[Thu Sep 10 16:58:53 GMT 2020] INFO Servidor iniciado en puerto 4444
[Thu Sep 10 16:59:11 GMT 2020] INFO Cliente conectado /172.17.0.1:44818
[Thu Sep 10 16:59:15 GMT 2020] INFO Cliente conectado /172.17.0.1:44822
.....
```
Perfecto... Ahora vamos a comprobar que pasa cuando "matamos" el contenedor actual. Para ello vamos a cortar el proceso del contenedor con CTRL+C. 
Ahora revisemos que no aparezca en estado RUNNING 
```bash
$docker container ps
```
Deberíamos entonces eliminarlo. Para ello obtenemos su ID
```bash
$docker container ps -all
CONTAINER ID        IMAGE                          COMMAND                CREATED             STATUS                            PORTS               NAMES
2b69cf17a259        tutorial-red-servidor:latest   "java Servidor 4444"   6 hours ago         Exited (130) About a minute ago                       tutorial-red-servidor
```
Y más tarde lo eliminamos
```bash
$docker container rm 2b69cf17a259
```
Cómo podemos ver, el volumen sigue creado y el "tail -f" sigue "escuchando" por nuevas escrituras.
Ahora si volvemos a crear el contenedor en cuestión, montamos el mismo volumen, debería seguir escribiendo en él.

```bash
$ docker run --name tutorial-red-servidor-nuevocontainer -v servidor-log:/usr/src/app -p 4444:4444 tutorial-red-servidor:latest
```
En el terminal del "tail -f" podemos ver que se actualizó la información
```bash
[Thu Sep 10 17:21:04 GMT 2020] INFO Servidor iniciado en puerto 4444
[Thu Sep 10 17:21:08 GMT 2020] INFO Cliente conectado /172.17.0.1:45034
[Thu Sep 10 17:21:13 GMT 2020] INFO Cliente conectado /172.17.0.1:45038
```
Próxima pregunta, ¿Y si pongo más de un contenedor que escribe en el mismo "log"? Vamos a probar agregando un segundo contendor. Abrimos otra pestaña o terminal y agregamos un nuevo contenedor con la misma imagen. Aspectos a tener en cuenta:
* Hay que poner otro nombre porque obviamente no pueden existir dos contenedores con la misma denominación
* Hay que cambiar el puerto de bind del Host anfitrión. Recuerden que todas las comunicaciónes en redes están sobre TCP/IP y solo podemos asignar un proceso a un puerto (IP:PUERTO). Esto significa que si yo estoy escuchando en un localhost:4444 no puedo poner un segundo servicio en ese puerto. ¿Solución? Simple, ponerlo a escuchar en otro puerto de host (no toco nada del contenedor). ¿Cómo? A través del flag -p 4443:4444.
```bash
$ docker run --name tutorial-red-servidor-nuevovecino -v servidor-log:/usr/src/app -p 4443:4444 tutorial-red-servidor:latest
```
Bien, primero veamos los contenedores que están corriendo
```bash
$ docker container ps
CONTAINER ID        IMAGE                          COMMAND                CREATED             STATUS              PORTS                    NAMES
0150061042a6        tutorial-red-servidor:latest   "java Servidor 4444"   47 seconds ago      Up 46 seconds       0.0.0.0:4443->4444/tcp   tutorial-red-servidor-nuevovecino
763e5a987063        tutorial-red-servidor:latest   "java Servidor 4444"   8 minutes ago       Up 8 minutes        0.0.0.0:4444->4444/tcp   tutorial-red-servidor-nuevocontainer
```

Una vez realizado, podemos probar "pegarle" a los servidores través de:
```bash
$ nc localhost 4444
```
y 
```bash
$ nc localhost 4443
```
Verificando que ambos responden registran en el mismo archivo de log.
```bash
....
[Thu Sep 10 16:59:11 GMT 2020] INFO Cliente conectado /172.17.0.1:44818
[Thu Sep 10 16:59:15 GMT 2020] INFO Cliente conectado /172.17.0.1:44822
[Thu Sep 10 17:21:04 GMT 2020] INFO Servidor iniciado en puerto 4444
[Thu Sep 10 17:21:08 GMT 2020] INFO Cliente conectado /172.17.0.1:45034
[Thu Sep 10 17:21:13 GMT 2020] INFO Cliente conectado /172.17.0.1:45038
[Thu Sep 10 17:28:38 GMT 2020] INFO Servidor iniciado en puerto 4444
....
```
¿Y si quiero manipular el volumen en el host?
Manteniendo el "tail -f", en otra pestaña intentemos acceder a la "carpeta" del directorio del volúmen. Sin ser root sucede que:
```bash
$ cd /var/lib/docker/volumes/servidor-log/
bash: cd: /var/lib/docker/volumes/servidor-log/: Permission denied
```
En cambio si entramos como root:
```bash
# cd /var/lib/docker/volumes/servidor-log/
# cd _data
# ls
info.log  Servidor.class  Servidor.java
```
Pudimos verificar que encontramos el archivo de log. De hecho vamos a agregarle contenido y ver que esto se escribe correctamente y que el "tail -f" lo recibe.
```bash
echo "desde el host anfitrion" >> info.log 
```
Bien, vimos varias cosas respecto de los volúmenes y los contenedores... Pero no vimos nada desde "dentro" de un contenedor. Por lo tanto, vamos a conectarnos a uno de nuestros servidores y ver que pasa ahí con nuestro volumen.

Primero, vamos a conectarnos a la consola bash (/bin/bash) del contenedor. Esto sería como hacer un "SSH" al mismo.
```bash
$ docker container ps
CONTAINER ID        IMAGE                          COMMAND                CREATED             STATUS              PORTS                    NAMES
0150061042a6        tutorial-red-servidor:latest   "java Servidor 4444"   2 hours ago         Up 2 hours          0.0.0.0:4443->4444/tcp   tutorial-red-servidor-nuevovecino
763e5a987063        tutorial-red-servidor:latest   "java Servidor 4444"   2 hours ago         Up 2 hours          0.0.0.0:4444->4444/tcp   tutorial-red-servidor-nuevocontainer
```
Elegimos, por ejemplo, a tutorial-red-servidor-nuevocontainer y nos conectamos
```bash
$ docker container exec -it tutorial-red-servidor-nuevocontainer /bin/bash
bash-4.2#
```
El parámetro "-it" permitirá levantar una terminal interactiva (en vez de ejecutar un comando) para que podamos acceder a la consola del contenedor. Específicamente el último parámetro "/bin/bash" indicará al contenedor que la consola que queremos utilizar será bash (ya que podría haber otras disponibles cómo sh, dash, entre otras)
Para más información acerca de la ejecución de comandos dentro de la consola, dirigirse al sitio oficial de docker (https://docs.docker.com/engine/reference/commandline/exec/)

Entonces, Una vez dentro, podemos realizar diversos comandos básicos para ver el estado y los paquetes instalados en dicha distribución.
Recuerden que siempre se va a tratar de instalar la menor cantidad posible de paquetes para:
a) No incrementar el tamaño del contenedor
b) Al instalar un paquete en un contenedor corriendo (en base a una imagen) los cambios solo quedarán reflejados en este contenedor, no en la imagen base.  Por lo tanto, si despliego un nuevo contenedor basado en la imagen fuente, los contenedores no serán iguales.
c) No habilitar herramientas innecesarias que pueden dañar el resto de la red de contenedores (Imágenes root, SSH client, etc)

* Podemos verificar que es un linux por su estructura de directorio
```bash
bash-4.2# cd /
bash-4.2# ls
bin   dev  home  lib64	mnt  proc  run	 srv  tmp  var
boot  etc  lib	 media	opt  root  sbin  sys  usr
```

* Vamos a comenzar por averiguar la versión de nuestro sistema operativo.
```bash
root@a8df97dd5cf1:/# cat /etc/os-release
NAME="Oracle Linux Server"
VERSION="7.8"
ID="ol"
ID_LIKE="fedora"
VARIANT="Server"
VARIANT_ID="server"
VERSION_ID="7.8"
PRETTY_NAME="Oracle Linux Server 7.8"
ANSI_COLOR="0;31"
CPE_NAME="cpe:/o:oracle:linux:7:8:server"
HOME_URL="https://linux.oracle.com/"
BUG_REPORT_URL="https://bugzilla.oracle.com/"

ORACLE_BUGZILLA_PRODUCT="Oracle Linux 7"
ORACLE_BUGZILLA_PRODUCT_VERSION=7.8
ORACLE_SUPPORT_PRODUCT="Oracle Linux"
ORACLE_SUPPORT_PRODUCT_VERSION=7.8
```

* Somos root en el contenedor... Entonces podemos hacer lo que queramos ... Vamos a instalar un paquete solo a modo de ejemplo. En este caso es un centos, así que su manejador de paquetes es YUM.
```bash
# yum update -y ; yum install net-tools -y
...
util-linux.x86_64 0:2.23.2-63.0.1.el7                                                      
  xz.x86_64 0:5.2.2-1.el7                                                                    

Complete!
```
Ejecutamos por ejemplo un ifconfig y nos encontramos con la información de la ip del equipo
```bash
# ifconfig
eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.17.0.2  netmask 255.255.0.0  broadcast 172.17.255.255
        ether 02:42:ac:11:00:02  txqueuelen 0  (Ethernet)
....
```
* Bien, volvamos al tema del volumen. Nosotros definimos cuando armamos el Dockerfile para la imagen del servidor que el WORKDIR iba a ser /usr/src/app. Entonces, si hacemos un pwd (comando linux para saber en que directorio estamos parados en este momento) deberíamos poder corroborarlo. Además, si hacemos un ls, nos encontraríamos con los archivos que pasamos con COPY a nuestro contenedor.
```bash
# pwd
/usr/src/app
# ls
info.log  Servidor.class  Servidor.java
```
Ahora repitamos el proceso que hicimos por fuera para validar que el archivo de log compartido se actualiza.
```bash
# tail -f info.log
.....
[Thu Sep 10 17:21:13 GMT 2020] INFO Cliente conectado /172.17.0.1:45038
[Thu Sep 10 17:28:38 GMT 2020] INFO Servidor iniciado en puerto 4444
desde el host anfitrion

```

# Sección 5 -- ¿Y si administramos Docker con una GUI? --
Existen varias alternativas a la hora de buscar administrar gráficamente Docker. Podemos mencionar las siguientes como principales:
- [Portainer](https://www.portainer.io/)
- [Rancher](https://rancher.com/)
- [Kitematic](https://kitematic.com/)
- [Dockstation](https://dockstation.io/)

En nuestro caso vamos a utilizar Portainer, ya que es simple de integrar y configurar. Por otro lado, la información que presenta es bastante útil.

Portainer se puede instalar como contenedor o como herramienta independiente en el SO.
En este tutorial, instalaremos Portainer como un contenedor Docker :)

Antes de "instalarlo", descarguemos la imagen de Portainer desde DockerHub usando el comando docker pull.
```bash
$ docker pull portainer/portainer:latest
```
Luego tenemos que configurar algunos parámetros de "entorno" que permiten que la herramienta mantenga las configuraciones que definamos de manera persistente.

*--p* Puertos de escucha: Portainer ahora requiere que dos puertos tcp estén expuestos; 9000 y 8000. El 9000 ha sido históricamente el puerto desde el que servimos la interfaz de usuario. El puerto 8000 es un servidor de túnel SSH y se utiliza para crear un túnel seguro entre el agente y la instancia de Portainer.

*--name* Nombre que le daremos al contenedor

*--restart* Política de gestión cuando el contenedor sufra un reinicio en el HOST anfitrión. En este caso le definimos "always" para que siempre, independientemente de la razón o problema sobre el contenedor, se levante el contendor.

*--v* Volúmenes: Es un muy buen caso para aplicar lo que aprendimos recientemente de "volúmenes". Sin un volumen, cómo dijimos, la información del contenedor se perderá cuando se reinicie (por error o por deseo) y se perderán todos los datos y configuraciones almacenadas dentro del mismo. En este caso, perderemos las configuraciones hacia los HOSTS Docker y otros detalles. ¿Queremos eso? No, entonces debemos persistir esos datos en "algún lado" fuera del contenedor para "sobrevivir" al ciclo de vida del contenedor en cuestión. En este caso utilizamos un folder del host directamente para "montar" dentro del Portainer Container, cómo se detalla a continuación:
-v /path/on/host/data:/data portainer/portainer
donde del : tenemos
- hacia la izquierda el folder en nuestro host y
- hacia la derecha donde se va a montar el directorio del host dentro del contenedor 

También Portainer define un "volúmen" particular como se detalle a continuación: 

-v /var/run/docker.sock:/var/run/docker.sock

Algunos contenedores necesitan "enlazar" el montaje del archivo /var/run/docker.sock para poder funcionar. 
Pero la pregunta es ¿Qué es este archivo y por qué a veces lo utilizan los contenedores? 
Respuesta corta: es el socket Unix en el que está "escuchando" el demonio Docker de forma predeterminada, y la API de docker "llama" al demonio vía "ese canal". 
Para poder ejecutar "algo" en docker se requieren permisos de root o pertenencia a un grupo de Docker. (Recordar lo que hicimos al principio de agregar nuestro usuario al grupo de Docker)
En el caso de "enlazarlo" a un contenedor, lo que estamos haciendo es permitir que el container pueda comunicarse con el demonio desde "dentro". ¿Entonces en Portainer para que lo queremos? Usando la GUI Portainer, las solicitudes HTTP que realice el usuario se envían al demonio de Docker a través de docker.sock "montado".

<p align="center"> <img src="https://miro.medium.com/max/700/1*bKtsM045UOnZcU2QVqcAmg.png" width="350"/> </p> 
<!--<a href="https://miro.medium.com/max/700/1*bKtsM045UOnZcU2QVqcAmg.png" target="blank"> Cómo acceder al demonio de Docker vía socket Unix</a>-->

Nota de color: Se puede habilitar un socket TCP. ¿Para qué lo habilitaría?

El demonio de Docker puede escuchar las solicitudes de la API de Docker Engine a través de tres tipos diferentes de Socket: unix, tcp y fd.
Si necesita acceder al demonio de Docker de forma remota, se debe habilitar tcp Socket. Tenga en cuenta que la configuración predeterminada proporciona acceso directo no cifrado y no autenticado al demonio de Docker, y debe protegerse mediante el socket cifrado HTTPS integrado o colocando un proxy web seguro frente a él. Puede escuchar en el puerto 2375 en todas las interfaces de red con -H tcp: //0.0.0.0: 2375, o en una interfaz de red particular usando su dirección IP: -H tcp: //192.168.59.103: 2375. Es convencional usar el puerto 2375 para comunicaciones no cifradas y el puerto 2376 para comunicaciones cifradas con el demonio.



```bash
$ docker run -d -p 9000:9000 -p 8000:8000 --name portainer --restart always -v /var/run/docker.sock:/var/run/docker.sock -v /path/on/host/data:/data portainer/portainer
```




# Sección 6 -- Redes Básicas -- Docker compose
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

## Sección 7 -- Conectarse por SSH a un contenedor y debug de arquitectura
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
