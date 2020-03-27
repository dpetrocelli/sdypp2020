package C2_LU.Threads;

public class Hijo implements Runnable{
	int sleep;
	
	public Hijo (int sleep) {
		this.sleep = sleep;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		
			/*int id = (int) Thread.currentThread().getId();
			//Thread.sleep(this.sleep);
			int a = 10;
			for (int i=0; i<900000000; i++) {
				a=a+a*1*a;
			}
			*/
			int counter = 0; 
			boolean interrupcion = false; 
			while (!(interrupcion)) {
				System.err.println(" HOLA SOY THREAD: "+Thread.currentThread().getId()+" vuelta="+counter);
				counter++;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println(" ME MATARON ");
					interrupcion = true;
				}
			}
			
		 
	}

}
