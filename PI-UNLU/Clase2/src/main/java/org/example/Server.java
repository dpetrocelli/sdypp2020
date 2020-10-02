package org.example;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Hello world!
 *
 */
public class Server
{
    public Server(int port){
        /*
        Capa de Trasnporte: TCP / UDP
         */
        try{
            // En vez de levantar un socketserver, vamos a crear un "Registro RMI"
            // [STEP 0] - Crear server RMI
            Registry serverRMI = LocateRegistry.createRegistry(port); // while (true){}
            // Otro ejemplo sería con spring Boot (server web http) -> while (true){}
            System.out.println ("RMI Registry has been started on port: "+port);
            // [STEP 1] - Instanciar las clases implementadora de las interfaces RMI
            ServerRunner sr = new ServerRunner();
            ServerRunner sr2 = new ServerRunner();
            // MAP entre Runner y el RMI
            // [STEP 2] - Mapear (exportar) todos los servicios en un determinado NAT puerto
            RemoteInt serviceWeather = (RemoteInt) UnicastRemoteObject.exportObject(sr, 6666);
            RemoteInt serviceCar = (RemoteInt) UnicastRemoteObject.exportObject(sr2, 7777);

            // [STEP 3] - Hacer un "bind" de nombre a servicio --> este es el nombre que va a ver el cliente
            serverRMI.rebind("service-weather", serviceWeather);
            serverRMI.rebind("service-car", serviceCar);

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
