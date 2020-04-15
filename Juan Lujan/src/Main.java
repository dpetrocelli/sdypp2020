import java.sql.Timestamp;
import java.util.ArrayList;

public class Main {
	private static int max = 1000;
	
	public static void main (String args[]) {
		System.out.println("Cliente");
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		System.out.println("TS: " + ts);
		ArrayList<Thread> threads = new ArrayList<Thread>();
		Thread t = null;
		
		for (int i = 0; i < max; i++) {
			t = new Thread(new Cliente());
			//t.setDaemon(true);
			t.start();
			threads.add(t);
		}
		
		/*
		Thread.currentThread().yield();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		// Barrera
		try {
			for (int i = 0; i < threads.size(); i++) {
				((Thread)threads.get(i)).join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		// mas instrucciones
		ts = new Timestamp(System.currentTimeMillis());
		System.out.println("TS end " + ts);
		System.out.println("End");
	}
}
