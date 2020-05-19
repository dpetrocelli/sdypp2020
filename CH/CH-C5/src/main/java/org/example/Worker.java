package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;


public class Worker
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

    private final Logger log = LoggerFactory.getLogger(Worker.class);
    public Worker() {
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
            this.queueChannel.basicQos(1);
            final Gson convert = new Gson();
            while (true){
                try {
                    // [STEP 1] - Obtener "transacción"
                    GetResponse base = queueChannel.basicGet(queueName, false);
                    String msg = new String(base.getBody());
                    long deliveryTag = base.getEnvelope().getDeliveryTag();
                    MsgStructure transaction = convert.fromJson(msg, MsgStructure.class);

                    // [STEP 2] - Verificar ttl
                    if (System.currentTimeMillis() <= transaction.getTtl()){

                        // [STEP 3] - Verificar que Origen y/o Destino están disponibles
                        // Tipo operación (Transacción: ClienteOrigen-ClienteDestino ; Depósito: ClienteOrigen ; Extracción: ClienteOrigen)

                        // [STEP 3.1] - Busco token de ClienteOrigen (leo con ACK para que otro worker no entre)
                        GetResponse responseOrigin = queueChannel.basicGet("SD-"+transaction.cOrigin, true);
                        // opc 1 con contenido (token) / opc 2 null (no hay msg - no token)
                        if (!Objects.isNull(responseOrigin)){
                            // Si tenemos el token de origen,
                            // [STEP 3.2] - Verificar el tipo de transaccion
                            if (!transaction.getOperationType().startsWith("transa")){
                                // Operación depósito / Operación extracción
                                boolean ok = true;
                                if (transaction.getOperationType().startsWith("extracc")) {
                                    ok = this.verifyBalance(transaction.getAmount(), transaction.getcOrigin());
                                    // si no tiene saldo, lo pasa a false
                                }
                                if (ok) {
                                    // operar
                                    // 1. insertar el movimiento
                                    // 2. "calcular" balance
                                    this.doOperation(transaction.getcOrigin(), transaction.getAmount(), transaction.getOperationType());
                                    // 3. borrar la tarea (ya procesada)
                                    queueChannel.basicAck(deliveryTag,false);
                                }else {
                                    // en este momento no tengo saldo, pero el ttl aun puede esperar
                                    // reencolo la tarea en la cola
                                    // - para test queueChannel.basicNack(deliveryTag,false,true);
                                    // PARA TEST
                                    queueChannel.basicAck(deliveryTag,false);
                                    String reWrite = gson.toJson(transaction);
                                    this.queueChannel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, reWrite.getBytes());
                                }
                                // En ambos casos, tengo que devolver el token de origen (único que tenia)
                                queueChannel.basicPublish("", "SD-"+transaction.cOrigin, MessageProperties.PERSISTENT_TEXT_PLAIN, "token".getBytes());
                                // TERMINE
                            }else{
                                // Operación Transacción
                                // vamos a verificar que tengamos el token de destino
                                    GetResponse responseDestination = queueChannel.basicGet("SD-"+transaction.cDestination, true);

                                    if (!Objects.isNull(responseDestination)){
                                        // si se da esto, tenemos los 2 tokens (origen, destino)
                                        // 1. Se verifica que origen tenga saldo
                                        boolean ok = this.verifyBalance(transaction.getAmount(), transaction.getcOrigin());
                                        if (ok){
                                            // 2. Realizo la operación
                                            //      2.1 Hacer la extraccion de origen
                                            this.doOperation(transaction.getcOrigin(), transaction.getAmount(), "extract");
                                            //      2.2 Hacer el depósito en destino
                                            this.doOperation(transaction.getcDestination(), transaction.getAmount(), "deposit");
                                            // 3. "borro" o "doy concluida" la transacción (es decir ACK)
                                            this.queueChannel.basicAck(deliveryTag, false);
                                        }else {
                                            // 3. "devuelvo" o "no pude atender" la transacción (es decir NACK)
                                            //this.queueChannel.basicNack(deliveryTag, false, true);
                                            // - para test queueChannel.basicNack(deliveryTag,false,true);
                                            // PARA TEST
                                            queueChannel.basicAck(deliveryTag,false);
                                            String reWrite = gson.toJson(transaction);
                                            this.queueChannel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, reWrite.getBytes());
                                        }
                                        // 4. "escribo" "devuelvo" los dos token (origen / destino) nuevamente (token free)
                                        queueChannel.basicPublish("", "SD-"+transaction.cOrigin, MessageProperties.PERSISTENT_TEXT_PLAIN, "token".getBytes());
                                        queueChannel.basicPublish("", "SD-"+transaction.cDestination, MessageProperties.PERSISTENT_TEXT_PLAIN, "token".getBytes());

                                    }else{
                                        // Tengo token de origen y no tengo token de destino
                                        // 1 - Devuelvo el token en origen
                                        queueChannel.basicPublish("", "SD-"+transaction.cOrigin, MessageProperties.PERSISTENT_TEXT_PLAIN, "token".getBytes());
                                        // 2 - Reencolo la transacción porque me faltó un token
                                        //queueChannel.basicNack(deliveryTag,false,true);
                                        // - para test queueChannel.basicNack(deliveryTag,false,true);
                                        // PARA TEST
                                        queueChannel.basicAck(deliveryTag,false);
                                        String reWrite = gson.toJson(transaction);
                                        this.queueChannel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, reWrite.getBytes());
                                    }
                            }
                        }else{
                            // Como no tenía ningún token, solo reencolo la tarea
                            //queueChannel.basicNack(deliveryTag,false,true);
                            // - para test queueChannel.basicNack(deliveryTag,false,true);
                            // PARA TEST
                            queueChannel.basicAck(deliveryTag,false);
                            String reWrite = gson.toJson(transaction);
                            this.queueChannel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, reWrite.getBytes());
                        }
                    }else{
                        // Se venció el TTL, tabla de notificaciones para que luego el cliente lea
                        this.notifyProblems(transaction.getOperationType(), transaction.getcDestination(), transaction.getAmount());
                        // id msg, ttl vencido
                        //queueChannel.basicAck(deliveryTag, false);
                        // - para test queueChannel.basicNack(deliveryTag,false,true);
                        // PARA TEST
                        queueChannel.basicAck(deliveryTag,false);
                        String reWrite = gson.toJson(transaction);
                        this.queueChannel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, reWrite.getBytes());

                    }
                    // PEDIR MSG ClientOrigin



                }catch (Exception e) {
                    try {

                        Thread.sleep(1000000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }

            }
            /*this.queueChannel.basicConsume(this.queueName, true, "myConsumerTag", new DefaultConsumer(this.queueChannel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body)
                        throws IOException {
                    // WHILE TRUE () this.queChannel.basicGet();
                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    long deliveryTag = envelope.getDeliveryTag();





                }
            });*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyProblems(String operationType, String getcDestination, double amount) {
    }

    private void doOperation(String user, double amount, String operationtype) {
        this.mdbc.createConnection();
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

        try {
            String sql = "insert into operations (user, operationtype, amount, date) values ("+Integer.parseInt(user)+", '"+operationtype+"', "+amount+", '"+ sqlDate+"');";
            this.mdbc.st.executeQuery(sql);
            // 4 - Actualizo mi balance (cliente)
            if (operationtype.startsWith("ext")){
                sql = "update client set balance=balance-"+amount+" where id=" + Integer.parseInt(user) + ";";
            }else{
                sql = "update client set balance=balance+"+amount+" where id=" + Integer.parseInt(user) + ";";
            }

            this.mdbc.st.executeQuery(sql);
            this.mdbc.st.close();
            this.mdbc.conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private boolean verifyBalance(double amount, String user) {
        String sql = "select balance from client where user='"+Integer.parseInt(user)+"';";
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
            this.mdbc.st.close();
            this.mdbc.conn.close();
        } catch (Exception e) {
            System.out.println (" ERROR" + e.getMessage());
        }

        return ok;
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
            this.queueChannel.queueDeclare("SD-david", true, false,false,null);
            this.queueChannel.queueDeclare("SD-nico", true, false,false,null);
            log.info(" Queue is ready to work");


            // MARIADB CONNECTION
            String dbname = "distributedProcessing";
		    String url = "jdbc:mariadb://" + mariadbHost + "/" + dbname;
		    String username = "david";
		    String password = "david";

		    this.mdbc = new MariaDBConnection(mariadbHost, dbname, username, url, password);
		    this.mdbc.createConnection();

            log.info(" MARIADB CONNECTED");
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Worker ct = new Worker();
    }


  }

