package C2_LU.TEst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Cliente {

	public static void main(String[] args) {
		
		
		try {
			// Paso 1 - Conectarse con el servidor
			// TCP capa 4, entonces como me conecto a un server?
			Socket s = new Socket ("127.0.0.1", 9000);
			
			// Paso 2 - Establecer los canales de comunicación
			// TCP en capa4 - 2 canales, canal de entrada (leer) ; canal de salida (escribir)
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			
			// Paso 3 - Enviar petición al servidor 
			canalSalida.println( "MSG DEL CLIENTE: "+s.getLocalPort());
			
			// Paso 4 - Recibir la respuesta
			String msgRespuesta = canalEntrada.readLine();
			
			System.out.println(" MSG RESPUESTA: "+msgRespuesta);
			
			// Paso 5 - Cerrar la conexión
			s.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
