
public class Sincro {

	public static void main(String args[]) {
		ObjetoCompartido obj = new ObjetoCompartido();
		
		// thread --> actualice obj
		// thread --> actualice obj
		System.out.println("Valor incial: " + obj.getDato());
		Thread t1 = new Thread(new SincroThread(obj));
		Thread t2 = new Thread(new SincoThreadV2(obj));
		t1.start();
		t2.start();
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Valor final: " + obj.getDato());
		System.out.println("End");
	}
}
