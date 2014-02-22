package flowmanager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: amauri
 * Date: 02/11/13
 * Time: 20:48
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionController {
    private String controller;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    private int delay=2;//delay milliseconds
    private int retries=3;//number or retries on reading

    public ConnectionController(String controller,int port){
        this.controller=controller;
        this.port=port;
    }

    public boolean connect(){
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(controller, port),100);
            //read the welcome message


            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        } finally {
            return true;
        }
    }
    public boolean send(String message){
        String answer="";
        //send
        try {
            out.writeBytes(message + "\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
        return true;
    }
    public String read(){
        String answer="";
        String inputLine;
        int n=0;
        while (n<retries){

            try {
                if (in.ready()){
                    answer=in.readLine();
                    while (in.ready()){
                        inputLine = in.readLine();
                        answer=answer+"\n"+inputLine;
                    }
                }
                else {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return new String("ERROR");
            }
            n++;
        }
        return answer;
    }
    public void close(){
        try {
            if (socket!=null){

                if (socket.isBound() || socket.isConnected()){
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
