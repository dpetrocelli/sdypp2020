package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteMain {

    public ClienteMain (int port){
        try {
            Gson gson = new Gson();
            Socket s = new Socket ("127.0.0.1", port);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pw = new PrintWriter (s.getOutputStream(),true);

            MensajeIntercambio msg = new MensajeIntercambio("subirTarea", "prueba1");
            String msgString = gson.toJson(msg);
            pw.println(msgString);
            pw.flush();

            String respuesta = br.readLine();
            System.out.println("RESPUESTA SERVER:  "+respuesta);

            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main( String[] args )
    {
        ClienteMain cm = new ClienteMain(9000);
    }
}
