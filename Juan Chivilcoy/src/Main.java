
public class Main {

	public static void main (String args[]) {
		ObjetoCompartido obj = new ObjetoCompartido();
		obj.setDato(5);
		
		System.out.println("Inicial: " + obj.getDato());
		Thread t1 = new Thread(new Wrk(obj, 2));
		Thread t2 = new Thread(new Wrk(obj, 3));
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Final: " + obj.getDato());
	}
}
