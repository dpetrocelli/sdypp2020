package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
            Gson gson = new Gson();
            // STEP 2 - Loop
            while (true){
                Socket s = ss.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter pw = new PrintWriter (s.getOutputStream(),true);

                String mensaje = br.readLine();
                MensajeIntercambio mi = gson.fromJson(mensaje, MensajeIntercambio.class);
                // HEADER - BODY
                System.out.println(" HEADER: "+mi.getHeader()+ " / BODY: "+ mi.getBody());

                if (mi.getHeader().equals("subirTarea")){

                    this.queueChannel.basicPublish("",this.jobQueue, MessageProperties.PERSISTENT_TEXT_PLAIN,  mi.getBody().getBytes());
                    log.info(" MSG SUBIDO A LA COLA ");
                }
                pw.println(" RECIBIDO ");
                pw.flush();

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
        ServerMain sm = new ServerMain(9000);
    }
}
