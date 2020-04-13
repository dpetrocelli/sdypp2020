import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Random;
/**
 * Servidor TCP simple. Cuando un cliente se conecta,
 * le envía la fecha y hora actual,
 * luego cierra la conexión.
 */
public class Servidor {
	public static void Log(String string, int random) {
		// Añadir fecha y hora
		String logString = "[" + new Date().toString() + "] INFO " + string;
		// Output por consola
		System.out.println(logString);
		// Output en archivo
		
		try (FileWriter fileWriter = new FileWriter("/tmp/javadir/info"+random+".log", true)) {
			fileWriter.write(logString + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) {

    	// Recibir el puerto por parámetro
		int port = Integer.parseInt(args[0]);
		Random r = new Random();
		int random =  r.nextInt((100 - 1) + 1) + 1;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Log("Servidor iniciado en puerto " + String.valueOf(port), random);
            while (true) {
            	// Aceptar conexiones de clientes
                try (
                	Socket clientSocket = serverSocket.accept();
                	PrintWriter out = 
                			new PrintWriter(clientSocket.getOutputStream(), true);
                ) {
                	Log("Cliente conectado " + clientSocket.getRemoteSocketAddress(), random);
                	out.println("Bienvenido al servidor de fecha y hora");
                    out.println(new Date().toString());
                }
            }
        } catch(IOException e) {
        	e.printStackTrace();
        }
    }
}
