package org.example;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public Client (int port){
        try{
            Registry clientRMI = LocateRegistry.getRegistry("127.0.0.1", port);
            String[] servicesList = clientRMI.list();

            for (String service : servicesList){
                System.out.println(" Service: "+service);
            }
            RemoteInt ri = (RemoteInt) clientRMI.lookup("service-weather");
            String weather = ri.getWeather("BSAS");
            System.out.println("WEATHER RMI: "+weather);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void main( String[] args )
    {
        // parametros de consola
        int port = 9090;
        Client client = new Client(port);
    }
}
