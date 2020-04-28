import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Worker implements Runnable {
	Socket s;
	
	public Worker (Socket s) {
		this.s = s;
	}
	
	@Override
	public void run() {
		BufferedReader canalEntrada;
		try {
			canalEntrada = new BufferedReader (new InputStreamReader (s.getInputStream()));
			PrintWriter canalSalida = new PrintWriter (s.getOutputStream(), true);
			System.out.println("Por leer del socket");
			String msg = canalEntrada.readLine();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Lei del socket");
			canalSalida.println(msg);

			System.out.println("2: Por leer del socket");
			msg = canalEntrada.readLine();
			System.out.println("2: Lei del socket");
			canalSalida.println(msg);
			
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
