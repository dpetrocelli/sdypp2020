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
	public static void Log(String string, String logfile) {
		// Añadir fecha y hora
		String logString = "[" + new Date().toString() + "] INFO " + string;
		// Output por consola
		System.out.println(logString);
		// Output en archivo
		
		try (FileWriter fileWriter = new FileWriter("/tmp/javadir/"+logfile+".log", true)) {
			fileWriter.write(logString + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) {

    	// Recibir el puerto por parámetro
		String nombre = System.getenv("nombre");
		String logfile = System.getenv("logName");
		int port = Integer.parseInt(System.getenv("port"));
		
		
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Log(nombre+" iniciado en puerto " + String.valueOf(port), logfile);
			
			while (true) {
            	// Aceptar conexiones de clientes
                try (
                	Socket clientSocket = serverSocket.accept();
                	PrintWriter out = 
                			new PrintWriter(clientSocket.getOutputStream(), true);
                ) {
                	Log("Cliente conectado " + clientSocket.getRemoteSocketAddress(), logfile);
                	out.println("Bienvenido al servidor de fecha y hora "+nombre);
                    out.println(new Date().toString());
                }
            }
        } catch(IOException e) {
        	e.printStackTrace();
        }
    }
}
