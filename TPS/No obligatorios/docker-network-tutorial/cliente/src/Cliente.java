import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Un cliente de línea de comandos para el servidor de fecha y hora.
 * Requiere la dirección (host y puerto) del servidor como argumento.
 * Termina luego de escribir la respuesta.
 */
public class Cliente {
    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try (
        	Socket clientSocket = new Socket(host, port);
    		BufferedReader in = new BufferedReader(
    				new InputStreamReader(clientSocket.getInputStream()));
        ) {
        	System.out.println("Servidor: " + in.readLine());
        	System.out.println("Servidor: " + in.readLine());
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}
