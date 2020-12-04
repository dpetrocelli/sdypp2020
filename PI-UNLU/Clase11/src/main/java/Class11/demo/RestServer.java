package Class11.demo;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

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
    ** administrador maestro -> código -> genere esa "política" -> Alta diponibilidad
    *** -> replicas
    ha-mode "exactly"
    ha-params 3
    ha-sync-mode "automatic"
    ha-promote-on-failure "when-synced"
    -> cómo obtener las policies vía CURL.
    curl -i -u user:bitnami http://localhost:15672/api/policies

    -> cómo setear una nueva policy (objeto json)
    curl -i -u user:bitnami -H "content-type:application/json" -X PUT -d'{"pattern":"sd*","definition":{"ha-mode":"exactly","ha-params":3,"ha-sync-mode":"automatic","ha-promote-on-failure":"when-synced"},"priority":1,"apply-to":"queues"}' http://localhost:15672/api/policies/%2f/ha-sd

    // REFERENCIA:
    https://www.cloudamqp.com/docs/http.html

    // hasta acá clase 9.
    // Clase 10, hicimos servicios en docker + balanceador
    // Clase 11 -> Hasta el momento, vimos
        // Socket Server
        // RMI
        // Web HTTP -> Spring Boot

    CLIENTE--> request -> hacer tarea X --> WEB/SOCKET SERVER   --> Almacena el file        <-- WORKER
                                                                --> Almacena el trabajo         <-- Esperando trabajo
                                                                                                    <-- obtiene trabajo
                                                                                                    ......
    PARTE 1: ** Server que reciba tareas
    1. Web Server / Socket Server
        /uploadFile -> PUT
        /downloadFile -> GET
    2. a) archivo b) parámetros -> video1 y quiero comprimirlo a 240p
    3. Guardarlo en la cola de trabajo
        -> estructura del trabajo {endpoint: http://server:8080/downloadFile?name="path"; parametros="480"}


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

@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST})

public class RestServer {
    Gson gson = new Gson();
    String BASEPATH = "src/main/resources/storage/";
    File storage = new File(BASEPATH);
    String ipRabbit;
    int portRabbit;
    String userRabbit;
    String passRabbit;
    String virtualHost;
    ConnectionFactory connectionFactory;
    Connection queueConnection;
    Channel queueChannel;
    String queueName;
    private final Logger log = LoggerFactory.getLogger(RestServer.class);

    public RestServer(){
        //[STEP 0] - Setear variables para el nombre de log
        //String logName = RestServer.class.getSimpleName()+"-"+Thread.currentThread().getId();
        //System.setProperty("log.name", logName);
        // [STEP 1] - Creación de storage
        if (!storage.exists()) {
            if (storage.mkdirs()) {
                System.out.println("Directorio creado"+storage.getAbsolutePath());

            } else {
                System.out.println("Error al crear directorio");
            }
        }

        // [STEP 2] - Conexión a RabbitMQ y funcionamiento
        log.info("Iniciando Servicio");
        this.ipRabbit = "127.0.0.1";
        this.portRabbit = 5672;
        this.userRabbit = "user";
        this.passRabbit = "bitnami";
        this.virtualHost = "%2F"; // para llamar a /
        this.queueName = "sd-2020";
        // Instanciar nuestra politica de HA
        this.applyHA(this.ipRabbit,this.userRabbit,this.passRabbit,this.virtualHost,"sd", "sd-*");
        //this.rabbitConnection();
    }

    private void applyHA(String ipRabbit, String userRabbit, String passRabbit, String virtualHost, String namePolicy, String patternPolicy) {
        //curl -i -u user:bitnami -H "content-type:application/json" -X PUT -d'{"pattern":"sd*","definition":{"ha-mode":"exactly","ha-params":3,"ha-sync-mode":"automatic","ha-promote-on-failure":"when-synced"},"priority":1,"apply-to":"queues"}' http://localhost:15672/api/policies/%2f/ha-sd
        String curlToRabbit = "curl -i -u ";
        curlToRabbit+=userRabbit+":"+passRabbit+" -H \"content-type:application/json\"";
        curlToRabbit+=" -X PUT -d'{\"pattern\":\""+patternPolicy+"\",\"definition\":{\"ha-mode\":\"exactly\",\"ha-params\":3,\"ha-sync-mode\":\"automatic\",\"ha-promote-on-failure\":\"when-synced\"},\"priority\":1,\"apply-to\":\"queues\"}' http://"+ipRabbit+":15672/api/policies/%2f/"+namePolicy;
        //String curlToRabbit = "curl -i -u user:bitnami -H \"content-type:application/json\" -X PUT -d'{\"pattern\":\"sd*\",\"definition\":{\"ha-mode\":\"exactly\",\"ha-params\":3,\"ha-sync-mode\":\"automatic\",\"ha-promote-on-failure\":\"when-synced\"},\"priority\":1,\"apply-to\":\"queues\"}' http://localhost:15672/api/policies/%2f/ha-sd";
        // [STEP 0] - Detect OS
        String os = System.getProperty("os.name").toLowerCase();

        String cmds="";
        // [STEP 1] - Depends on OS, create the correct command
        if (os.startsWith("lin")){
            cmds= "/bin/bash -c ";
        }else{
            cmds="CMD /C powershell.exe ";
        }
        //bin/bash -c file....
        String path = "/tmp/filebash.sh";
        /*
        FILE CREATOR
         */
        File fstream = new File(path);
        System.out.println("Trying to create script sh file....");
        try {
            // Create file for install JAVA JRE
            PrintStream out = new PrintStream(new FileOutputStream(fstream));
            out.println(curlToRabbit);


            out.close();
            System.out.println("File sh created successfully.....");
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        cmds+= path;
        //log.info(cmds+ "filebash.sh");
        // [STEP 2] - Execute Runtime.EXEC

        try {
            Process runner = Runtime.getRuntime().exec("chmod +rx "+path);
            runner.waitFor();
            runner = Runtime.getRuntime().exec(cmds);
            //Process runner = Runtime.getRuntime().exec(curlToRabbit);
            //[STEP 3] - Read line to line (from console)
            BufferedReader br = new BufferedReader(new InputStreamReader(runner.getInputStream()));
            String line;
            while ((line=br.readLine())!=null){
                log.info("LINE: "+line);
            }

            BufferedReader br2 = new BufferedReader(new InputStreamReader(runner.getErrorStream()));
            String line2;
            while ((line2=br2.readLine())!=null){
                log.info("LINE ERROR: "+line2);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


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

    private void rabbitDisconnection(){
        try{
            this.queueChannel.close();
            this.queueConnection.close();
            log.info(" Queue Session has been disconnected ");
        }catch (Exception e){

        }



    }

    // -> nombre y tipo del método en escucha (GET /test)
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    // Además le digo que estoy esperando una variable por "header" que es "name"
    //serverip:puerto/test?name="algo"
    public ResponseEntity<String> listJobs(@RequestParam("name") String nombre) throws InterruptedException {
        // ALGO ->
        return new ResponseEntity<String>(("Hola tu nombre es: "+nombre).trim(), HttpStatus.OK);
    }

    // CURL PARA SUBIDA
    // curl -H 'Content-Type: multipart/form-data' -F 'file=@sample.mp4' 'http://127.0.0.1:8080/uploadFile?paremetros=480'
    @PutMapping("/uploadJob")
    public ResponseEntity<String> uploadFile(@RequestParam(value = "parametros", required = false) String parametros, @RequestParam("file") MultipartFile file) {
        String fullName = file.getOriginalFilename();
        // STEP 1 - Guardar archivo
        try {
            Files.copy(file.getInputStream(), Paths.get(storage +"/"+ fullName), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("pude guardar el archivo");
        } catch (IOException e) {
            System.out.println("no pude guardar el archivo: "+e.getMessage());
            //e.printStackTrace();
        }
        // STEP 2 - Guardar trabajo

        try {
            this.rabbitConnection();
            DataObject data = new DataObject("http://127.0.0.1:8080/downloadFile?fileName=sample.mp4", parametros);
            String msg = gson.toJson(data);

            this.queueChannel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
            this.rabbitDisconnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<String>(("ok").trim(), HttpStatus.OK);
    }

    // CURL
    // curl 'http://127.0.0.1:8080/downloadFile?fileName=sample.mp4' -o tutest.mp4
    @GetMapping("/downloadFile")
    public ResponseEntity<UrlResource> downloadFile(@RequestParam("fileName") String fileName) {
        // Load file as Resource
        try {
            //String current = new java.io.File( "." ).getCanonicalPath();
            String current= storage + "/"+fileName;

            UrlResource resource;

            Path filePath = Paths.get(current);
            resource = new UrlResource(filePath.toUri());


            String contentType= Files.probeContentType(filePath);



            // Fallback to the default content type if type could not be determined
            if(contentType == null) {
                contentType = "application/octet-stream";
            }

           return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            System.out.println (" ERROR: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println (" ERROR: " + e.getMessage());
            return null;
        }


    }
    // CURL
    // curl 'http://127.0.0.1:8080/deleteFile?fileName=sample.mp4'
    @GetMapping("/deleteFile")
    public ResponseEntity<String>  deleteFile(@RequestParam("fileName") String fileName) {
        // Load file as Resource
        //String current = new java.io.File( "." ).getCanonicalPath();
        String current= storage + "/"+fileName;
        File file = new File(current);
        if (!file.exists()) {
            System.out.println(" File doesn't exist "+fileName);
            return new ResponseEntity<String>( " File doesn't exist" , HttpStatus.NOT_FOUND);
        } else {
            // SI EXISTE
            if(file.delete())
            {
                System.out.println("File deleted successfully");
                return new ResponseEntity<String>( " File deleted successfully" , HttpStatus.OK);
            }
            else
            {
                System.out.println("Failed to delete the file");
                return new ResponseEntity<String>( "Failed to delete the file" , HttpStatus.BAD_REQUEST);
            }

        }

    }


}
