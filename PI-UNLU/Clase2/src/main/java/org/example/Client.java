package org.example;

import com.google.gson.Gson;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    Gson gson = new Gson();
    public Client (int port){
        try{
            Registry clientRMI = LocateRegistry.getRegistry("127.0.0.1", port);
            String[] servicesList = clientRMI.list();

            for (String service : servicesList){
                System.out.println(" Service: "+service);
            }
            RemoteInt ri = (RemoteInt) clientRMI.lookup("service-weather");
            //String weather = ri.getWeather("BSAS");
            //System.out.println("WEATHER RMI: "+weather);

            int[] v1 = {1,2,3,4,5,6,7,8,9,10};
            int[] v2 = {1,2,3,4,5,6,7,8,9,10};
            int[] v3 = new int[0];
            ObjetosVectores ov = new ObjetosVectores();
            ov.setV1(v1);
            ov.setV2(v2);
            // miov <- resultaod del objeto del otro lado
            // clase cliente ov ---> copia ----> server
            // clase cliente ov <--- pise con la copia <--- server

            String jsonMsg = gson.toJson(ov,ObjetosVectores.class);
            jsonMsg = jsonMsg.substring(0,(jsonMsg.length()-1));
            jsonMsg+=",\"v3\"-[0,0,0,0,0,0,0,0,0,0]}";
            System.out.println(jsonMsg);
            ov = gson.fromJson(ri.sumaVectoresBasadaEnObjetos(jsonMsg),ObjetosVectores.class);
            System.out.println(mostrarv(ov.v3));



        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String mostrarv (int[] v){
        String mensaje = "";
        for (int k=0; k<v.length;k++){
            mensaje+= v[k]+"-";

        }
        return mensaje;
    }
    public static void main( String[] args )
    {
        // parametros de consola
        int port = 9090;
        Client client = new Client(port);
    }
}
