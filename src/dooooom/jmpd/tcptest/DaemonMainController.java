package dooooom.jmpd.tcptest;

import java.util.Map;

/**
 * Created by Philip on 4/23/2014.
 */
public class DaemonMainController implements Runnable,RequestController {
    public static void main(String[] args) {
        DaemonMainController dmc = new DaemonMainController();
        dmc.run();
    }

    public void run() {
        DaemonConnectionController dcc = new DaemonConnectionController(4444, this);

        Thread dccThread = new Thread(dcc);
        dccThread.start();
    }

    @Override
    public Map<String, Object> processRequest(Map<String, Object> request) {
        request.put("ack","ack");
        return request;
    }
}
