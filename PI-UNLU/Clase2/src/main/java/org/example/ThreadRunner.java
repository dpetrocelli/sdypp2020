package org.example;

public class ThreadRunner implements Runnable{
    int random;
    public ThreadRunner (int random){
        this.random = random;
    }
    @Override
    public void run() {
        try{
            long id = Thread.currentThread().getId();
            System.out.println(" CREAMOS EL THREAD: "+id);

            Thread.sleep(this.random);
            System.out.println(" TERMINÓ EL THREAD "+id);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
