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
Se puede revisar las imágenes descargadas con
```bash
$ docker images
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
Si ahora se accede al directorio definido en *Mountpoint*, se podrá leer el archivo de log
```
$ sudo less +F /var/lib/docker/volumes/servidor-log/_data/info.log
```
Otro uso útil para los volúmenes es montar un directorio del host dinámicamente en un contenedor. De esta manera, los cambios realizados en el host se actualizan en tiempo real en el contenedor. Por ejemplo, se podría editar y compilar el código fuente en Servidor.java en el host y este se actualizaría en el contenedor sin necesidad de volver a construir la imagen.
Para este ejemplo:
 - Probar con quitar la línea *RUN* en el Dockerfile
 - Compilar en el host (*javac Servidor.java*)
 - Construir la imagen nuevamente (*docker build -t tutorial-red-servidor-v2*)
 - Correr el contenedor con la opción *-v <ruta absoluta al proyecto>:<ruta del contenedor>*
 ```
$ docker run --name tutorial-red-servidor-v2 -v /home/usuario/docker-network-tutorial/tutorial/servidor/src:/usr/src/app -p 4444:4444 tutorial-red-servidor-v2:latest
```
Como última aclaración, cabe mencionar que los volúmenes pueden ser accedidos remotamente, compartidos entre contenedores y otras opciones más (consultar https://docs.docker.com/storage/volumes/)

Una vez utilizado, se puede eliminar con el comando rm.  Este comando rm es aplicable a todos los recursos docker (contenedores, redes, almacenamiento, etc). 
```bash
$ docker container rm tutorial-red-servidor
```

# Sección 5 -- Redes -- Docker compose
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
