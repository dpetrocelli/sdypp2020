package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    String mariadbHost;
    MariaDBConnection mdbc;
    private final Logger log = LoggerFactory.getLogger(Worker.class);

    public Worker() {

        try {
            // STEP 1 - Server Socket
            int intentos = 0;
            boolean condicion = false;

            while (!condicion) {
                condicion = this.crearConexion();
                intentos++;
                if (intentos > 4) condicion = true;
                Thread.sleep(500);
            }

            this.gson = new Gson();
            // STEP 2 - Loop

            int exitStatus;
            this.queueChannel.basicQos(1);
            while (true) {
                // [PASO 0] - LOOP de lectura de msgs
                exitStatus = this.obtainTransaction();

            }



        } catch (Exception e) {

        }
    }

    private int obtainTransaction() {
        int exitStatus = 1;
        try {
            // [PASO 1] - Leer un mensaje de la cola (transacciones)
            GetResponse msgStructure = this.queueChannel.basicGet(this.jobQueue, false);
            long idForAck = msgStructure.getEnvelope().getDeliveryTag();
            String bodyString = new String (msgStructure.getBody());
            Operation transaction = this.gson.fromJson(bodyString, Operation.class);
            // [PASO 2] - Verificar que el ttl de la transacción no se haya vencido
            boolean result = false;
            if (transaction.getTtl() > System.currentTimeMillis()){
                // TTL OK
                // [PASO 3] - Vamos a "hacer" la transacción que leimos
                 result = this.doTransaction (transaction);
            }else{
                // TTL VENCIDO
                // . Notificar al usuario
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                String sql = "insert into notifications (user, operation, amount, msg, date) values ('"+transaction.getcOrigin()+"', '"+transaction.getOperationType()+"', "+transaction.getAmount()+", '408 - ERROR TTL Vencido', '"+ sqlDate+"');";
                System.out.println (" notif: "+sql);
                try{
                    this.mdbc.createConnection();
                    this.mdbc.st.executeQuery(sql);
                    this.mdbc.closeConnection();
                    result = true;
                    log.info(" El TTL de la operación estaba vencido ");
                }catch (Exception e){
                    e.printStackTrace();
                }

            }


            if (result){
                this.queueChannel.basicAck(idForAck, false);
            }else{
                // "pongo en pausa" el reencolar original
                        //this.queueChannel.basicNack(idForAck,false,true);
                // Y para "reencolar" al final de la cola ("solo a modo demo")
                    // 1. Publica la tarea (que va a ir al final de la cola
                    // 2. Si no fallo 1, ACK de la tarea original (Borrarlo)
                try{
                    // 1 publicación
                    this.queueChannel.basicPublish("", this.jobQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, bodyString.getBytes());
                    // 2 eliminación (ACK)
                    this.queueChannel.basicAck(idForAck, false);
                }catch (Exception e){
                    this.queueChannel.basicNack(idForAck,false,true);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return  exitStatus;
    }

    private boolean doTransaction(Operation transaction) {
        boolean result = true;
        boolean balanceOK = false;
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
                // Ver tipo de operacion.
                // 2 clientes (transaccion)
                // 1 cliente (depósito / extracción)
                if (transaction.getOperationType().startsWith("depo") || transaction.getOperationType().startsWith("extra")){
                    // con 1 token (origen) es suficiente, puedo operar
                    // deposito -> (+)
                    // extraccion -> (-)
                    balanceOK = true;
                    if (transaction.getOperationType().startsWith("extra")){
                        balanceOK = this.verifyBalance (transaction.getcOrigin(), transaction.getAmount());
                    }

                    if (balanceOK){
                        this.doOperation (transaction.getcOrigin(),transaction.getAmount(), transaction.getOperationType());
                    }else{
                        result = false;
                    }
                    this.queueChannel.basicNack(tokenOrigin.getEnvelope().getDeliveryTag(), false, true);
                }else{
                    // ES TRANSACCION
                    // verificar token destino.
                    // if token destino = 1 ; tengo token
                    // if toekn destino = 0 ; no tengo token -> false operation
                    GetResponse tokenDestination = this.queueChannel.basicGet("lusd-" + transaction.getcDestination(), false);
                    if (Objects.isNull(tokenDestination)){
                        // token origin si ; token destination no
                        this.queueChannel.basicNack(tokenOrigin.getEnvelope().getDeliveryTag(), false, true);
                        result = false;
                    }else{
                        // TOKEN ORIGIN si ; TOKEN DESTINATION si
                        // VALIDATE balance origen (-)

                        balanceOK = this.verifyBalance (transaction.getcOrigin(), transaction.getAmount());

                        if (balanceOK){
                            // balance origen   -=transaction.getAmount();
                            // balance dest     +=transaction.getAmount();
                            this.doOperation (transaction.getcOrigin(),transaction.getAmount(), "extraccion");
                            this.doOperation (transaction.getcDestination(),transaction.getAmount(), "deposito");
                        }else{
                            result = false;
                        }


                        this.queueChannel.basicNack(tokenOrigin.getEnvelope().getDeliveryTag(), false, true);
                        this.queueChannel.basicNack(tokenDestination.getEnvelope().getDeliveryTag(), false, true);
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void doOperation(String user, double amount, String operationType) {
        //  1. insertar en movimientos

        //  2. Update del balance cliente
            // si operacion depósito (+)
            // si operación extraccion (-)
        this.mdbc.createConnection();
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

        try {
            String sql = "insert into operations (user, operationtype, amount, date) values ('"+user+"', '"+operationType+"', "+amount+", '"+ sqlDate+"');";
            this.mdbc.st.executeQuery(sql);
            // Actualizo mi balance (cliente)
            if (operationType.startsWith("ext")){
                sql = "update client set balance=balance-"+amount+" where user='" +user + "';";
            }else{
                sql = "update client set balance=balance+"+amount+" where user='" + user+ "';";
            }

            this.mdbc.st.executeQuery(sql);
            this.mdbc.closeConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private boolean verifyBalance(String user, double amount) {
        // if balance > monto , ok
        String sql = "select balance from client where user='"+user+"';";
        boolean ok = false;
        ResultSet result = null;
        this.mdbc.createConnection();
        try {
            result = this.mdbc.st.executeQuery(sql);
            if(result.next()) {
                double balance = result.getDouble("balance");
                // verificar que el saldo es suficiente
                if (balance>=amount) {
                    ok =  true;
                }
            }
            this.mdbc.closeConnection();
        } catch (Exception e) {
            System.out.println (" ERROR" + e.getMessage());
        }

        return ok;

    }


    private boolean crearConexion() {
        boolean result = false;
        try {
            // ESTRUCTURA DE CONEXION CON RABBIT
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


            // ESTRUCTURA DE CONEXION CON MARIADB
            String dbname = "distributedProcessing";
            this.mariadbHost = "af0c97f9cb85140dd8ca759fd1b3ca5b-1051178316.us-east-1.elb.amazonaws.com";
            String url = "jdbc:mariadb://" + mariadbHost + "/" + dbname;
            String username = "david";
            String password = "david";

            this.mdbc = new MariaDBConnection(mariadbHost, dbname, username, url, password);
            this.mdbc.createStructure();
            log.info(" MariaDB structure and connection ok ");


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
