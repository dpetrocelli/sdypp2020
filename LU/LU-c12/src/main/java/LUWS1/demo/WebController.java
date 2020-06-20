package LUWS1.demo;

import com.jcraft.jsch.*;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.rest.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;

@RestController
public class WebController {
    Azure azure;
    JSch jsch;
    public WebController(){
        try{
            final File credFile = new File("/home/soporte/my.azureauth");
            this.azure = Azure.configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();
            System.out.println(" CONECTADOS A AZURE");
            this.jsch = new JSch();
        }catch (Exception e){
            System.err.println(" CONECTADOS A AZURE");
        }

    }
    // Spring Initializr
    //https://start.spring.io/
    @GetMapping("isWorking")
    public ResponseEntity<Boolean> working() throws InterruptedException {
        System.out.println ("LLEGO UNA PETICION");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK); //200 ok
        //return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST); // 404 400 500
    }

    @GetMapping("listvms")
    public ResponseEntity<ArrayList<String>> listvms() throws InterruptedException {
        System.out.println ("LLEGO list vms");
        ArrayList<String> listVms = new ArrayList<String>();
        VirtualMachines lvm = this.azure.virtualMachines();

        PagedList<VirtualMachine> b = lvm.list();
        for (VirtualMachine vm: b) {
            try{
                System.out.println ( " VM: "+vm.name()+" / SIZE: "+vm.size());
                listVms.add(vm.name());
            }catch (Exception e){

            }

        }

        return new ResponseEntity<ArrayList<String>>(listVms, HttpStatus.OK); //200 ok
        //return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST); // 404 400 500
    }

    @GetMapping("poweroffvm")
    public ResponseEntity<Boolean> poweroffvm() throws InterruptedException {
        System.out.println ("poweroff");
        this.azure.virtualMachines().powerOff("bcra_mgc", "linuxlu");
        System.out.println ("poweroff done");
        return new ResponseEntity<Boolean>(true, HttpStatus.OK); //200 ok
        //return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST); // 404 400 500
    }

    @GetMapping("createvm")
    public ResponseEntity<Boolean> creavm() throws InterruptedException {
        VirtualMachine vm = this.azure.virtualMachines().getByResourceGroup("bcra_mgc", "linuxvm2");
        PublicIPAddress b = vm.getPrimaryPublicIPAddress();
        String ip = b.ipAddress().toString();
        String path = "/home/soporte/Documents/unlu/sdypp2020/LU/LU-c12/target/demo-0.0.1-SNAPSHOT.jar";
        // COPY JAR
        //this.copyTo(ip, path);
        // RUN JAR
        this.execute(ip);

        // 1 ) previo tenemos que tener el jar disponible
        // 2 ) JSCH
        // 3 ) instalar java
        // 4 ) copiar el jar
        // 5 ) correr el jar (IP:PUERTO) <-- internet
        return new ResponseEntity<Boolean>(true, HttpStatus.OK); //200 ok
        //return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST); // 404 400 500
    }

    private void copyTo(String ip, String path) {
        try{
            Session session = jsch.getSession("AzureUser", ip, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("Password.0101");
            
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            // Cast to ChannelSFTP
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(path, "/home/AzureUser/server.jar");
            System.out.println("Elements coppied to Azure Node");
        }catch (Exception e){
            
        }
      
    }

    private void execute(String hostname) {
        try{
            // [STEP 1] - SSH CONNECTION
            Session session = jsch.getSession("AzureUser", hostname, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("Password.0101");

            session.connect();

            // [STEP 2] - Define CONNECTION Type
            System.out.println("EXEC SESSION CONNECTED .....");
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");


            // Gets an InputStream for this channel. All data arriving in as messages from the remote side can be read from this stream.
            InputStream in = channelExec.getInputStream();

            // Set the command that you want to execute
            // In our case its the remote shell script
            //String commandExec = "sh "+filename;

            // [STEP 3] - Define commands
            channelExec.setCommand("sudo -s apt update ; sudo -s apt install docker.io -y -f ; docker run --name pruebalocal -p 8080:8080 dpetrocelli/prueba:latest ");
            //sudo -s apt install default-jre -y -f ; java -jar /home/AzureUser/server.jar");
            //&& sudo -s apt-get install vlc -y -f");
            channelExec.connect();


            // Read the output from the input stream we set above
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;

            //Read each line from the buffered reader and add it to result list
            // You can also simple print the result here
            try {

                while ((line = reader.readLine()) != null)
                {
                    System.out.println(line);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.err.println("END OF EXEC");
            //retrieve the exit status of the remote command corresponding to this channel
            int exitStatus = channelExec.getExitStatus();

            //Safely disconnect channel and disconnect session. If not done then it may cause resource leak
            channelExec.disconnect();
            session.disconnect();

            if(exitStatus < 0){
                 System.out.println("Done, but exit status not set!");
            }
            else if(exitStatus > 0){
                 System.out.println("Done, but with error!");
            }
            else{ // CASO 0 OK
                System.out.println("Done!");
            }
        }catch (Exception e){

        }

    }


    // 1 - por la web vamos a crear una vm - linux - ssh
        // 1. a través del SDK Java (software development kit)
        // API para que vos puedas utilizar los recuersos de ese servicio
        // SDK (API) Azure -> pom - dependencias maven (portal vs codigo)
    // 2 - copiar al server virtual ssh un proyecto (copiar un jar) + copiar un script bash (java)
        // Desde consola: ssh -p usuario@equipo / scp -P filelocal@servidor:/directorioremoto
        // Desde java:
                // Alt 1: Process Builder / Runtime .exec ("comandos") ; ("file.sh/ps1")
                // Alt 2: jsch - Java Secure Channel - pom - maven
    // 3 - empaquetar como dijimos el jar
    // 4 - deploy en docker local
    // 5 - deploy en kubernetes (cloud) - IP PUBLICA + MDB
    // 6 - ffmpeg desde consola
    // 7 - ffmpeg desde java (comprimir un video X en una calidad Y) - // Alt 1: Process Builder
    // 8 - empaquetado de java con ffmpeg (parámetros) para comprimir deployado en un K8s
    // 9 - WebServer diga quien lo resuelve W1 ; W2 ; W3; + LB IP PUB - suba el file al algún lugar
}
