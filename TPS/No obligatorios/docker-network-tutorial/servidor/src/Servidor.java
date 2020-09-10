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
    	// [STEP 1] - Recibir el puerto por parámetro
    	int port = Integer.parseInt(args[0]);
        // [STEP 2] - Crear un "Servidor Socket" (Podría ser un servidor web o cualquier otra cosa) 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // [STEP 3] - Llamar a la clase LOG y pasarle la información (puerto)
            Log("Servidor iniciado en puerto " + String.valueOf(port));
            // [STEP 4] - Crear un loop "para siempre"
            while (true) {
            	// Aceptar conexiones de clientes
                try {
                    // [STEP 5] - Aceptar un cliente 
                	Socket clientSocket = serverSocket.accept();
                    // [STEP 6] - Mandar a la clase LOG la información del cliente
                    Log("Cliente conectado " + clientSocket.getRemoteSocketAddress());
                    // [STEP 7] - Abrir el canal de salida para poder escribirle la respuesta
                	PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("Bienvenido al servidor de fecha y hora");
                    out.println(new Date().toString());
                }catch(IOException e) {
                    e.printStackTrace();
                }
            
            
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
