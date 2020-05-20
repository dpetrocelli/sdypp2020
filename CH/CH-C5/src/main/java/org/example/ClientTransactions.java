package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;


public class ClientTransactions
{
    ConnectionFactory cf;
    Connection conn;
    Channel queueChannel;
    String queueName;
    String ipRabbit = "";
    //int portRabbit ;
    String user = "admin";
    String pwd = "admin";
    String mariadbHost;
    MariaDBConnection mdbc;

    private final Logger log = LoggerFactory.getLogger(ClientTransactions.class);
    public ClientTransactions () {
        try {
            
            BufferedReader br;
            PrintWriter pw;
            Gson gson = new Gson();
            // [STEP 0] - Rabbit Connection
            this.ipRabbit = "a16e64467fa6b4bde9adaf78edc87fb1-992766720.us-east-1.elb.amazonaws.com";
            user = "admin";
            pwd = "admin";
            this.queueName = "SD-TransferQueue";
            this.mariadbHost = "af0c97f9cb85140dd8ca759fd1b3ca5b-1051178316.us-east-1.elb.amazonaws.com";

            this.createConnection();
            // [STEP 1] - Create transaction
            long millis=System.currentTimeMillis();  
            millis+=600000;
            // 100 minuto de vida la transacción

            MsgStructure transaction = new MsgStructure("1", "2", 100, "transaction", millis);
            Gson convert = new Gson();
            String msg = convert.toJson(transaction);
            this.queueChannel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());

            transaction = new MsgStructure("2", "1", 400, "transaction", millis);
            convert = new Gson();
            msg = convert.toJson(transaction);
            this.queueChannel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());

            transaction = new MsgStructure("1", "", 400, "deposit", millis);
            convert = new Gson();
            msg = convert.toJson(transaction);
            this.queueChannel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
            log.info(" transacción publicada en la cola");

            /*
            while (true) {
                s = ss.accept();
                br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                pw = new PrintWriter (s.getOutputStream(),true);
                log.info(" CHANNELS CREATED");
                String msg = br.readLine();
                log.info(" RAW MSG: "+msg);
                ExchangeMsg realMsg = gson.fromJson(msg, ExchangeMsg.class);
                log.info(" MSG RECEIVED: "+realMsg.getHeader());

                if (realMsg.getHeader().startsWith("getTask")) {
                    log.info(" GET TASK : "+realMsg.getHeader() + " / ");
                    // 1ra forma leer sin revisar si hay contenido
                    // 2da más linda
                    if (this.queueChannel.queueDeclarePassive(this.queueName).getMessageCount()>0){
                        log.info(" HAY MSG");
                        GetResponse data = this.queueChannel.basicGet(this.queueName,true);
                        // ID MSG
                        long idMsg = data.getEnvelope().getDeliveryTag();
                        //this.queueChannel.basicAck(idMsg,false);

                        // SI PROCESO OK
                        //this.queueChannel.basicAck(idMsg,false);
                        // SI PROCESO NO OK
                        //this.queueChannel.basicNack(idMsg, false, true);
                        String content = new String (data.getBody());
                        log.info(" QUEUE MSG "+content);

                    }
                    realMsg.setBody("CONTENIDODECOLA");
                    msg = gson.toJson(realMsg);
                    pw.write(msg);
                    pw.flush();

                }else {
                    if (realMsg.getHeader().startsWith("publishTask")){
                        log.info(" publish TASK : "+realMsg.getHeader() + " / "+realMsg.getBody());
                        // COLA

                        this.queueChannel.basicPublish("", this.queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, realMsg.getBody().getBytes());
                        log.info( " MSG is saved in SD Queue");
                        pw.write("HTTP 200 OK");
                        pw.flush();

                    }else{

                    }
                }
                s.close();
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createConnection() {


        try {
            // RABBIT CONNECTION
            this.cf = new ConnectionFactory();
            this.cf.setHost(this.ipRabbit);
            this.cf.setUsername(this.user);
            this.cf.setPassword(this.pwd);
            log.info (" Rabbit connection OK");

            this.conn = this.cf.newConnection();
            log.info (" Rabbit direct Connection OK");

            this.queueChannel = this.conn.createChannel();
            log.info (" Rabbit queue Channel is ready to work ");

            this.queueChannel.queueDeclare(this.queueName, true, false,false,null);
            log.info(" Queue is ready to work");
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ClientTransactions ct = new ClientTransactions();
    }


  }

