import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A command line client for the date server. Requires the IP address of the
 * server as the sole argument. Exits after printing the response.
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
