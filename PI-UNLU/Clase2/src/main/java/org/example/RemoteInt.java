package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInt extends Remote {
    public String getWeather (String city) throws RemoteException;
    public int[] sumaVectoresOriginal (int[]v1, int[] v2) throws RemoteException;
    public String sumaVectoresBasadaEnObjetos (String ov) throws RemoteException;
}
