package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Hello world!
 *
 */
public class App 
{
    private final Logger log = LoggerFactory.getLogger(App.class);
    public App (){
        String os = System.getProperty("os.name").toLowerCase();
        log.info(" OS: "+ os);

        //String cmd = "sudo apt update";
        // CMD PARA WINDOWS
        // String cmd = "dir c:/";
        String path = this.createScript();
        this.runCmdCommands (os, path);
    }

    private String createScript() {
        String filename = "/home/soporte/installer.sh";
        // Powershell .ps1
        try {
            File file = new File(filename);
            PrintStream out = new PrintStream(new FileOutputStream(file));
            out.println("ls /home/soporte/Documents");
            out.println("ls /homopiy");
            out.close();
        }catch (Exception e){

        }
        return filename;
    }

    private void runCmdCommands(String os, String cmd) {
        ProcessBuilder pb = new ProcessBuilder();
        try{
            if (os.startsWith("linux")){
                String cmds[] = {"/bin/bash", "-c", cmd};
                pb.command(cmds);
            }else{
                String cmds[] = {"CMD", "/C", "powershell.exe "+cmd};
                pb.command(cmds);
            }
            Process runner = pb.start();
                BufferedReader input = new BufferedReader (new InputStreamReader(runner.getInputStream()));
                String line;
                while ((line = input.readLine())!=null){

                    log.info(" LINEA INPUT: "+line);

                }

                BufferedReader error = new BufferedReader (new InputStreamReader(runner.getErrorStream()));
                String line2;
                while ((line2 = error.readLine())!=null){

                    log.info(" LINE ERROR: "+line2);

                }
                // JOIN
            runner.waitFor();
        }catch (Exception e){

        }

    }

    public static void main( String[] args )
    {
        int threadId = (int) Thread.currentThread().getId();
        String logName = App.class.getSimpleName()+"-"+threadId;
        System.setProperty("log.name", logName);
        App app = new App();
    }
}
