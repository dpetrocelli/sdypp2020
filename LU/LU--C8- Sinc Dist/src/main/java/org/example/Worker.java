package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class Worker
{
    String ipRabbit;
    int portRabbit;
    String usuarioRabbit;
    String passRabbit;
    ConnectionFactory connectionFactory;
    String jobQueue;
    Connection queueConnection;
    Channel queueChannel;
    String user;
    Gson gson;
    private final Logger log = LoggerFactory.getLogger(Worker.class);

    public Worker() {

        try {
            // STEP 1 - Server Socket
            int intentos = 0;
            boolean condicion = false;

            while (!condicion) {
                condicion = this.crearConexionConRabit();
                intentos++;
                if (intentos > 4) condicion = true;
            }

            this.gson = new Gson();
            // STEP 2 - Loop

            int exitStatus;
            this.queueChannel.basicQos(1);
            while (true) {

                exitStatus = this.obtainTransaction();

            }



        } catch (Exception e) {

        }
    }

    private int obtainTransaction() {
        int exitStatus = 1;
        try {
            GetResponse msgStructure = this.queueChannel.basicGet(this.jobQueue, false);
            long idForAck = msgStructure.getEnvelope().getDeliveryTag();
            String bodyString = new String (msgStructure.getBody());
            Operation transaction = this.gson.fromJson(bodyString, Operation.class);
            boolean result = this.doTransaction (transaction);

            if (result){
                this.queueChannel.basicAck(idForAck, false);
            }else{
                this.queueChannel.basicNack(idForAck,false,true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return  exitStatus;
    }

    private boolean doTransaction(Operation transaction) {
        boolean result = false;
        // ORIGIN / DESTINATION -> verify if token is free
        // token free => 1 msg -> obtuve token - puse 0
        // token occuped => 0 msg
        //if (getmsgcolaorigen  != null)
        try {
            GetResponse tokenOrigin = this.queueChannel.basicGet("lusd-" + transaction.getcOrigin(), false);
            // MSG o NO MSG -> if 1, había token y lo tomé
            //              -> if 0, chau, devolver trasnaccion (no la puedo atender)
            if (Objects.isNull(tokenOrigin)){
                // msg 0, no token -> no puedo operar
                result = false;
            }else{
                // msg 1, tengo token origen.
                // verificar token destino.
                // if token destino = 1 ; tengo token
                // if toekn destino = 0 ; no tengo token -> false operation
                GetResponse tokenDestination = this.queueChannel.basicGet("lusd-" + transaction.getcDestination(), false);
                if (Objects.isNull(tokenDestination)){
                    // token origin si
                    this.queueChannel.basicNack(tokenOrigin.getEnvelope().getDeliveryTag(), false, true);
                    // token destination no
                    result = false;
                }else{
                    // TOKEN ORIGIN ; TOKEN DESTINATION
                    // VALIDATE balance origen (-)
                    // balance origen   -=transaction.getAmount();
                    // balance dest     +=transaction.getAmount();
                    result = true;
                    this.queueChannel.basicNack(tokenOrigin.getEnvelope().getDeliveryTag(), false, true);
                    this.queueChannel.basicNack(tokenDestination.getEnvelope().getDeliveryTag(), false, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private int createTransaction(Scanner teclado) {
        int status = 1;
        log.info("ingrese usuario destino");
        String destination="";
        while (destination.length()<1){
          destination = teclado.nextLine();
        }


        log.info ("ingrese monto");
        double amount = teclado.nextDouble();
        Operation op = new Operation();
        op.setcOrigin(this.user);
        op.setcDestination(destination);
        op.setAmount(amount);
        op.setOperationType(1);
        op.setTtl(System.currentTimeMillis()+10000000);
        String opJson = this.gson.toJson(op);
        try {
            this.queueChannel.basicPublish("", this.jobQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, opJson.getBytes());
            status = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    private boolean crearConexionConRabit() {
        boolean result = false;
        try {
            this.ipRabbit= "a16e64467fa6b4bde9adaf78edc87fb1-992766720.us-east-1.elb.amazonaws.com";
            //this.portRabbit
            this.usuarioRabbit = "admin";
            this.passRabbit = "admin";
            this.jobQueue = "lusd-transactionQueue";
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
        Worker sm = new Worker();
    }
}
