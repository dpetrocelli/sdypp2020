package org.example;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
Objetivos:
* 1 - Tener el stack de Rabbit -> Levantado
    ** usar de la carpeta rabbit el despliegue del docker-compose
* 2 - Configurar Logback para manejo de logs (archivo)
    ** importar librerías desde maven -> pom.xml
    ** /src/main/resources -> logback.xml. Esto define como va a guardar los archivos de log (log/${log.name}.log)
    ** En cada clase debemos "instanciar" un Logger
        *** private final Logger log = LoggerFactory.getLogger(Server.class);
        *** Definimos un nombre -> genere el archivo con ese nombre
* 3 - Trabajar con Rabbit
    ** importar librerías desde maven -> pom.xml
    ** Todos los datos de Rabbit para poder conectarnos (método URI)
    ** Conectarse -> ConnectionFactory --> queueConnection --> queueChannel --> queueDeclare --> queueBasicPublish

    PARA PROXIMA CLASE
    ** administrador maestro -> código -> genere esa "política"
    *** -> replicas


    CLIENTE--> request -> hacer tarea X --> WEB/SOCKET SERVER   --> Almacena el file        <-- WORKER
                                                                --> Almacena el trabajo         <-- Esperando trabajo
                                                                                                    <-- obtiene trabajo
                                                                                                    ......
    PARTE 1: ** Server que reciba tareas
    1. Web Server / Socket Server
        /recibirTrabajo
    2. a) archivo b) parámetros -> video1 y quiero comprimirlo a 240p
    3. Guardarlo en la cola de trabajo 
        -> estructura del trabajo
        -> archivo ? FTP

    PARTE 2: Workers que están "escuchando" por tareas
        1. estar escuchadno si hay algo en la cola
        2. descarga el archivo (?) y aplica filtros (sobel p4)
        3. procesa
        4. sube resultados 
            -> mensaje a la cola diciendo OK termine
            -> archivo modificado (imagen, video, -----)

    *** -> asignar tareas:
        **** Dilan, instalar filezilla (FTP)
        **** descargar de FTP archivo de video
        **** comprimirlo en un worker
        **** subir el archivo comprimido

*/
public class Server {
    String ipRabbit;
    int portRabbit;
    String userRabbit;
    String passRabbit;
    String virtualHost;
    ConnectionFactory connectionFactory;
    Connection queueConnection;
    Channel queueChannel;
    String queueName;

    private final Logger log = LoggerFactory.getLogger(Server.class);

    public Server(int port) {
        log.info("Iniciando Servicio");
        this.ipRabbit = "127.0.0.1";
        this.portRabbit = 5672;
        this.userRabbit = "user";
        this.passRabbit = "bitnami";
        this.virtualHost = "%2F"; // para llamar a /
        this.queueName = "sd-2020";
        this.rabbitConnection();

    }

    private void rabbitConnection() {

        try {
            this.connectionFactory = new ConnectionFactory();
          
            String uri = "amqp://"+this.userRabbit+":"+this.passRabbit+"@"+this.ipRabbit+":"+this.portRabbit+"/"+this.virtualHost;
            log.info("URI:"+uri);
            
            this.connectionFactory.setUri(uri);
            log.info("Connection factory has been configured"); 
            this.queueConnection = this.connectionFactory.newConnection();
                       
            log.info(" Queueconnection OK");
            this.queueChannel = this.queueConnection.createChannel();
            log.info(" Queue Channel OK ");

            this.queueChannel.queueDeclare(this.queueName, true, false, false, null);

            this.queueChannel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, "hola".getBytes());
            this.queueChannel.close();
            this.queueConnection.close();
            log.info(" Queue Session has been disconnected ");

            
        } catch (IOException | TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        // parametros de consola
        int threadId = (int) Thread.currentThread().getId();
        String logName = Server.class.getSimpleName()+"-"+threadId;
        System.setProperty("log.name", logName);
        int port = 9090;
        Server server = new Server(port);
    }
}
