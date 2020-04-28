package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hello world!
 *
 */
public class ServerMain
{
    String ipRabbit;
    int portRabbit;
    String usuarioRabbit;
    String passRabbit;
    ConnectionFactory connectionFactory;
    String jobQueue;
    Connection queueConnection;
    Channel queueChannel;

    private final Logger log = LoggerFactory.getLogger(ServerMain.class);

    public ServerMain (int port){
        try {
            // STEP 1 - Server Socket
            int intentos = 0 ;
            boolean condicion = false;

            while (!condicion){
                condicion = this.crearConexionConRabit ();
                intentos++;
                if (intentos>4) condicion = true;
            }



            ServerSocket ss = new ServerSocket (9000);
            log.info("Server is Running on port: 9000");
            Gson gson = new Gson();
            // STEP 2 - Loop
            while (true){
                Socket s = ss.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter pw = new PrintWriter (s.getOutputStream(),true);

                String mensaje = br.readLine();
                ExchangeMsg realMsg = gson.fromJson(mensaje, ExchangeMsg.class);
                // HEADER - BODY
                System.out.println(" HEADER: "+realMsg.getHeader()+ " / BODY: "+ realMsg.getBody());

                if (realMsg.getHeader().startsWith("getTask")) {
                    log.info(" GET TASK : "+realMsg.getHeader() + " / ");
                    // 1ra forma leer sin revisar si hay contenido
                    // 2da más linda
                    if (this.queueChannel.queueDeclarePassive(this.jobQueue).getMessageCount()>0){
                        log.info(" HAY MSG");
                        GetResponse data = this.queueChannel.basicGet(this.jobQueue,true);
                        // ID MSG
                        long idMsg = data.getEnvelope().getDeliveryTag();
                        //this.queueChannel.basicAck(idMsg,false);

                        // SI PROCESO OK
                        //this.queueChannel.basicAck(idMsg,false);
                        // SI PROCESO NO OK
                        //this.queueChannel.basicNack(idMsg, false, true);
                        String content = new String (data.getBody());
                        log.info(" QUEUE MSG "+content);
                        realMsg.setBody(content);

                    }else{
                        realMsg.setBody("empty");
                    }
                    String msg = gson.toJson(realMsg);
                    pw.println(msg);
                    pw.flush();


                }else {
                    if (realMsg.getHeader().startsWith("publishTask")){
                        log.info(" publish TASK : "+realMsg.getHeader() + " / "+realMsg.getBody());
                        // COLA

                        this.queueChannel.basicPublish("", this.jobQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, realMsg.getBody().getBytes());
                        log.info( " MSG is saved in SD Queue");
                        pw.write("HTTP 200 OK");
                        pw.flush();

                    }else{

                    }
                }
               

                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private boolean crearConexionConRabit() {
        boolean result = false;
        try {
            this.ipRabbit= "52.188.44.231";
            //this.portRabbit
            this.usuarioRabbit = "admin";
            this.passRabbit = "admin";
            this.jobQueue = "SD-javaTest";
            this.connectionFactory = new ConnectionFactory();
            this.connectionFactory.setHost(this.ipRabbit);
            this.connectionFactory.setUsername(this.usuarioRabbit);
            this.connectionFactory.setPassword(this.passRabbit);
            log.info(" RabbitMQ Connection has alredy established ");

            this.queueConnection = this.connectionFactory.newConnection();
            log.info(" Queue Connection OK ");

            this.queueChannel = this.queueConnection.createChannel();
            log.info(" Queue Channel ready to work ");

            this.queueChannel.queueDeclare(this.jobQueue,true, false, false, null);

            result = true;
        }catch (Exception e ){
           log.error(e.getMessage());
        }

        return result;
    }

    public static void main( String[] args )
    {
        int thread = (int) Thread.currentThread().getId();
        String packetName=ServerMain.class.getSimpleName().toString()+"-"+thread;
        System.setProperty("log.name",packetName);
        ServerMain sm = new ServerMain(9000);
        
    }
}
