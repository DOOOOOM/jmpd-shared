package dooooom.jmpd.tcptest;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ClientMainController implements ResponseController,Runnable {
    ClientConnectionController ccc;
    int n;

    public static void main(String[] args) {
        ClientMainController cmc = new ClientMainController();
        cmc.run();
    }

    public void run() {
        ccc = new ClientConnectionController("localhost",4444,this);

        Thread cccThread = new Thread(ccc);
        cccThread.start();

        //To allow initial connection to be established
        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String,Object> testRequest = new HashMap<String,Object>();
                testRequest.put("test"+n,"test"+n+"value");
                ccc.sendMap(testRequest);
                n++;
            }
        }, 1000, 1000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

    }

    @Override
    public void processResponse(Map<String, Object> request, Map<String, Object> response) {
        System.out.println("[INFO]    request: " + request + " response: " + response);
    }

    @Override
    public void onConnect() {
        System.out.println("[INFO]    Connection established");
        System.out.flush();

        Map<String,Object> testRequest = new HashMap<String,Object>();
        testRequest.put("test","testvalue");
        ccc.sendMap(testRequest);
    }

    @Override
    public void onDisconnect() {
        System.out.println("[INFO]    Connection lost");
    }

    @Override
    public void giveStatusInformation(String s) {
        System.out.println("[INFO]    Status update: " + s);
    }
}
