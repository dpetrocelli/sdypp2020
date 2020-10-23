package org.example;

import com.google.gson.Gson;

import java.rmi.RemoteException;

public class ServerRunner implements RemoteInt{
    Gson gson = new Gson();

    @Override
    public String getWeather(String city) throws RemoteException {
        return "Cloudy-17º";
    }

    @Override
    public int[] sumaVectoresOriginal(int[] v1, int[] v2) throws RemoteException {
        int[] v3 = new int[10];
        for (int i =0;i<v1.length;i++){
            v3[i]= v1[i]+v2[i];
            v1[i] = 0;
            v2[i] = 0;
        }

        return v3;
    }

    @Override
    public String sumaVectoresBasadaEnObjetos(String ovx) throws RemoteException {
        // [STEP 0] - convierto de string (json) -> Objeto -> ObjetosVectores
        ObjetosVectores ov = gson.fromJson(ovx, ObjetosVectores.class);
        System.out.println(" V3 - SUB 1 : "+ov.getV3()[1]);
        //int[] v3 = new int[10];
        //ov.setV3(v3);
        // [STEP 1] - Opero como objeto normal
        for (int i =0;i<ov.v1.length;i++){
            ov.v3[i]= ov.v1[i]+ov.v2[i];
            ov.v1[i] = 0;
            ov.v2[i] = 0;
        }
        // [STEP 2] - convertir el objeto (ObjetosVectores) a JSON String
        String objetoJSONString = gson.toJson(ov);

        return objetoJSONString;
    }

    public String getDate() throws RemoteException {
        return "Monday";
    }
}
