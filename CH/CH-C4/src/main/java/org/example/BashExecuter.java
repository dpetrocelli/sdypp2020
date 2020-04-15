package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BashExecuter {

    private final Logger log = LoggerFactory.getLogger(BashExecuter.class);
    
    public BashExecuter (){
        String os = System.getProperty("os.name").toLowerCase();
        log.info(" OS : "+os);
        FileWriter fichero = null;
        PrintWriter pw = null;
        this.createShellScript();
       
        //String cmd = "ls -l";
        this.runCmd (os, "/home/soporte/installer.sh");
    }
    
    private String createShellScript() {
		String filename = "/home/soporte/installer.sh";
		File fstream = new File(filename);
		System.out.println("Trying to create script sh file....");
		try {
			// Create file for install JAVA JRE
			PrintStream out = new PrintStream(new FileOutputStream(fstream));
			//out.println("mkdir /home/soporte/testfromjava");
            //out.println("touch  /home/soporte/testfromjava/archivo1");
            //out.println ("echo cotnenidoaarchivo >  /home/soporte/testfromjava/salida");
			//out.println ("sudo apt update");

			// Close the output stream
			//out.close();
			System.out.println("File sh created successfully.....");
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return filename;

	}
    private void runCmd(String os, String cmd) {
        String totalLines="";
        try {
                   
                
            if (os.startsWith("lin")){
                //runner = Runtime.getRuntime().exec("/bin/bash -c "+cmd);
                String cmds[] = {"/bin/bash", "-c", cmd};
                /*ProcessBuilder pb = new ProcessBuilder(cmds);
                Process runnerBuilder = pb.start();*/
                Process runnerBuilder = Runtime.getRuntime().exec(cmds);
                BufferedReader br = new BufferedReader (new InputStreamReader (runnerBuilder.getInputStream()));
                String line;

                while ((line = br.readLine())!= null) {
                    log.info(" LINEA ENTRADA:" + line);
                }

                BufferedReader br2 = new BufferedReader (new InputStreamReader (runnerBuilder.getErrorStream()));
                String line2;

                while ((line2 = br2.readLine())!= null) {
                    log.info(" LINEA ENTRADA:" + line2);
                }

            }else{
                String cmds[] = {"CMD","/C","powershell.exe ls"};

                //String cmds[] = {"cmd", "\\c", "ls"};
                Process runner = Runtime.getRuntime().exec(cmds);
                BufferedReader br = new BufferedReader (new InputStreamReader (runner.getInputStream()));
                String line;

                while ((line = br.readLine())!= null) {
                    log.info(" LINEA ENTRADA:" + line);
                }

                
                
            }
         
           
            
        } catch (Exception e) {
            log.error(" EXPLOTO "+e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        int thread = (int) Thread.currentThread().getId();
        String packetName=BashExecuter.class.getSimpleName()+"-"+thread;
        System.setProperty("log.name",packetName);
        BashExecuter be = new BashExecuter();
    }
}