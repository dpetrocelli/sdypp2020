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
import java.util.Objects;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class ClientTransaction
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
    private final Logger log = LoggerFactory.getLogger(ClientTransaction.class);

    public ClientTransaction(String user) {
        this.user = user;
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
            boolean salir = false;
            Scanner teclado = new Scanner(System.in);
            while (!salir) {
                log.info(" Ingrese opción");
                log.info(" 1 - Realizar transacción ");
                log.info(" 9 - Salir ");

                int transaction = teclado.nextInt();
                if (transaction==1){
                    int exitStatus = this.createTransaction(teclado);
                    if (exitStatus==0){
                        log.info(" Su transacción se registró con éxito");

                    }else{
                        log.error(" transacción no completada");
                    }
                }else{
                    salir= true;
                }

            }
            this.queueChannel.close();
            this.queueConnection.close();



        } catch (Exception e) {

        }
    }

    private int createTransaction(Scanner teclado) {
        int status = 1;
        log.info("ingrese usuario destino");
        String destination="";
        while (destination.length()<1){
          destination = teclado.nextLine();
        }

        log.info("ingrese tipo de operación (transferencia, depósito, extracción");
        String operationType="";
        while (operationType.length()<1){
            operationType = teclado.nextLine();
        }
        log.info ("ingrese monto");
        double amount = teclado.nextDouble();
        Operation op = new Operation();
        op.setcOrigin(this.user);
        op.setcDestination(destination);
        op.setAmount(amount);
        op.setOperationType(operationType);
        op.setTtl(System.currentTimeMillis()+1000000);
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

            try{
                GetResponse obj = this.queueChannel.basicGet("lusd-" + user, false);
                if (!Objects.isNull(obj)){
                    this.queueChannel.basicNack(obj.getEnvelope().getDeliveryTag(), false, true);
                }

            }catch (Exception e){
                this.queueChannel = this.queueConnection.createChannel();
                this.queueChannel.queueDeclare("lusd-" + user, true, false, false, null);
                this.queueChannel.basicPublish("", "lusd-" + user, MessageProperties.PERSISTENT_TEXT_PLAIN, "token".getBytes());
            }


            result = true;
        }catch (Exception e ){
           log.error(e.getMessage());
        }

        return result;
    }

    public static void main( String[] args )
    {
        Scanner teclado = new Scanner(System.in);
        System.out.println(" Ingrese su nombre de usuario ");
        String user =  teclado.nextLine();
        ClientTransaction sm = new ClientTransaction(user);
    }
}
