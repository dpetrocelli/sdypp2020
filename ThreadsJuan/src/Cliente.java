import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {

	public static void main(String[] args) {
		
		
		try {
			// Paso 1 - Conectarse con el servidor
			// TCP capa 4, entonces como me conecto a un server?
			System.out.println("Cliente: por conectarse");
			Socket s = new Socket ("127.0.0.1", 9000);
			System.out.println("Cliente: conexion establecida");
			
			// Paso 2 - Establecer los canales de comunicación
			// TCP en capa4 - 2 canales, canal de entrada (leer) ; canal de salida (escribir)
			BufferedReader canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			
			// Paso 3 - Enviar petición al servidor 
			Scanner sc = new Scanner(System.in);
			System.out.println("Ingrese el mensaje a enviar");
			String msg = sc.nextLine();
			canalSalida.println("MSG DEL CLIENTE: " + msg);
			//canalSalida.flush();
			System.out.println("Mensaje enviado");
			
			// Paso 4 - Recibir la respuesta
			String msgRespuesta = canalEntrada.readLine();
			System.out.println(" MSG RESPUESTA: "+msgRespuesta);
			
			System.out.println("2: Ingrese el mensaje a enviar");
			msg = sc.nextLine();
			canalSalida.println("2: MSG DEL CLIENTE: " + msg);
			
			// Paso 4 - Recibir la respuesta
			 msgRespuesta = canalEntrada.readLine();
			System.out.println("2: MSG RESPUESTA: "+msgRespuesta);

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
