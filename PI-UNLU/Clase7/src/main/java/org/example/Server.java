package org.example;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * prueba de EFK con Java
 * 1. Tener el servicio (esta clase)
 * 2. Convertir las clases a -> .Jar -> maven (lib, dep, etc) -> mvn clean package
 * 3. --> ./target/ex....jar
 * 4. Con nuestro dockerfile -> FROM img original + copy del JAR + EXP puerto
 * 5. docker build -> hacer la imagen final -> para que luego lo podamos correr
 * 6. docker run del container
 */
public class Server
{
    public Server(int port){
        /*
        Capa de Trasnporte: TCP / UDP
         */
        try{
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server has started on port "+port);


            while (true){
                Socket client = ss.accept();
                /*
                Particular de java como maneja la E/S del socket ->
                                                    Canal de Entrada -> server (lee)
                                                    Canal de Salida -> server  (escribe)
                --> Canal
                    * String <-- JSON <-- TEXT
                    * Buffer
                    * Object Serializable (JAVA) --> Public class Auto implements Serializable {}
                 */
                System.out.println("Atendiendo al cliente: "+client.getPort());

                // 3 pasos
                // 1er paso
                ServerHilo sh = new ServerHilo(client);
                // 2do paso
                Thread serverThread = new Thread(sh);
                // 3er paso
                serverThread.start();



            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public static void main( String[] args )
    {
        // parametros de consola
        int port = 9090;
        Server server = new Server(port);
    }
}
