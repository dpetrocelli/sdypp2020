package org.example;

import java.rmi.RemoteException;

public class ServerRunner implements RemoteInt{
    @Override
    public String getWeather(String city) throws RemoteException {
        return "Cloudy-17º";
    }

    public String getDate() throws RemoteException {
        return "Monday";
    }
}
