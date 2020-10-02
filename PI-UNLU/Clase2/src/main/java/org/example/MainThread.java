package org.example;


import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class MainThread {

    public static void main( String[] args )
    {
        ArrayList<Thread> remainingThreads = new ArrayList<Thread>();

        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000 + 1);
        ThreadRunner tr = new ThreadRunner(randomNum);
        Thread trThread = new Thread(tr);
        //Thread.State state = trThread.getState();
        //String stringState = state.name();
        //System.out.println("[THR] - State: "+stringState);
        trThread.start();

        //System.out.println("[THR] - State: "+trThread.getState().name());
        remainingThreads.add(trThread);

        randomNum = ThreadLocalRandom.current().nextInt(1000, 10000 + 1);
        ThreadRunner tr2 = new ThreadRunner(randomNum);
        Thread tr2Thread = new Thread(tr2);
        tr2Thread.start();
        remainingThreads.add(tr2Thread);
        /*
        ThreadRunner tr3 = new ThreadRunner(randomNum);
        Thread tr3Thread = new Thread(tr3);
        tr3Thread.start();
        remainingThreads.add(tr3Thread);
        System.out.println(" MAIN - Thread iniciado");
        */
        try {
            boolean finished = false;
            while (remainingThreads.size()>0){

                for (Thread t : remainingThreads){
                    //t.join();
                    if (trThread.getState().name().startsWith("TER")){
                        System.out.println("[THR] - EL THREAD "+t.getId()+" SE MURIO, entonces lo borro de mi lista de pendientes:");
                        remainingThreads.remove(t);
                    }else{
                        //System.out.println("[THR] - EL THREAD" +t.getId()+" X sigue vivo, no podemos terminar el programa principal :");
                    }

                }
                Thread.sleep(1000);
            }

            System.out.println(" MAIN - Thread finalizado");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // guardar el resultaod en una dB
    }
}
