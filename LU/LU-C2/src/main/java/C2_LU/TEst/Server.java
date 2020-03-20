package C2_LU.TEst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hello world!
 *
 */

public class Server 
{
	String nombre;
	
    public static void main( String[] args )
    {
    	// Cliente / Servidor  
    	// 2 procesos, 1 proceso servidor (escuchando)
    	// 1 proceso cliente (conn - Server)
    	// 1 Server - TCP - Capa 4 --- IP: IP ; TCP : Puerto
    	
    	try {
			// PASO 1 - Crear servidor (puerto)
    		ServerSocket ss = new ServerSocket (9000);
			System.out.println( " OK FUNCIONANDO en el peurto 9000");
			
			// Paso 2 (repetitivo)
			while (true) {
				// aceptar clientes
				Socket cliente = ss.accept();
				System.out.println(" CLIENTE RECIBIDO: "+cliente.getInetAddress().getCanonicalHostName() + " : " +cliente.getPort());
				
				// COMO HACER UN THREAD
				// 1 - Instanciar la clase
				ServerHijo sh = new ServerHijo(cliente);
				// 2 - Crear un thread y decirle que va a ejecutar con la instancia X
				Thread shThread = new Thread(sh);
				// 3 - Ejecutar el hilo (start)
				shThread.start();
				
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println( " PUERTO EN USO");
			//e.printStackTrace();
		} 
    	
      
    }
}
