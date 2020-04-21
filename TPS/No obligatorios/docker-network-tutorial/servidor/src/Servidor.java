import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Servidor TCP simple. Cuando un cliente se conecta,
 * le envía la fecha y hora actual,
 * luego cierra la conexión.
 */
public class Servidor {
	public static void Log(String string) {
		// Añadir fecha y hora
		String logString = "[" + new Date().toString() + "] INFO " + string;
		// Output por consola
		System.out.println(logString);
		// Output en archivo
		try (FileWriter fileWriter = new FileWriter("info.log", true)) {
			fileWriter.write(logString + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) {
    	// Recibir el puerto por parámetro
    	int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Log("Servidor iniciado en puerto " + String.valueOf(port));
            while (true) {
            	// Aceptar conexiones de clientes
                try (
                	Socket clientSocket = serverSocket.accept();
                	PrintWriter out = 
                			new PrintWriter(clientSocket.getOutputStream(), true);
                ) {
                	Log("Cliente conectado " + clientSocket.getRemoteSocketAddress());
                	out.println("Bienvenido al servidor de fecha y hora");
                    out.println(new Date().toString());
                }
            }
        } catch(IOException e) {
        	e.printStackTrace();
        }
    }
}
