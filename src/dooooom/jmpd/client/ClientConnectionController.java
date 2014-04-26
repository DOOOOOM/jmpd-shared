package dooooom.jmpd.client;

import dooooom.jmpd.data.JsonParser;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;

/**
 * This controller class is intended to store connection state information for the daemon,
 * as well as handling all daemon calls themselves.  These daemon calls should be presented
 * as public non-static methods that will act according to the state information stored
 * in an instance of this class.
 */
public class ClientConnectionController implements Runnable {
    private Socket socket;

    private String host;
    private int port;

    /*
     * Keep track of how many times an automatic reconnect has been attempted.
     * If more that maxReconnectTries, do not try again.
     */
    private int reconnectTries = 0;
    private final int maxReconnectTries = 5;

    /*
     * Since sometimes the socket.isConnected() returns true even when a connection is failing,
     * after seeing a connection error this will be flagged as false until reconnect() succeeds.
     */
    private boolean connected = false;

    ResponseController rc;

    private List<Map<String,Object>> requests = new ArrayList<Map<String,Object>>();
    int nextUID = 1;

    public ClientConnectionController(String host, int port, ResponseController rc) {
        this.host = host;
        this.port = port;
        this.rc = rc;
    }

    @Override
    public void run() {
        //establish initial connection
        reconnect();

        while(true) {
            if(socket != null && socket.isConnected()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String s = in.readLine();

                    System.out.println("[DEBUG]   Received: " + s);

                    Map<String,Object> response;

                    response = JsonParser.stringToMap(s);

                    String uid = (String) response.get("request_id");

                    Map<String, Object> request = null;

                    if (uid.equals("0")) {
                        /*
                         * request_id:0 is a special case reserved for
                         * the server sending unsolicited information to the client
                         */
                        request = new HashMap<String, Object>();
                        request.put("command","INFO");
                        request.put("request_id","0");
                    } else {
                        int requestsFound = 0;

                        for (Map<String, Object> request_i : requests) {
                            if (request_i.get("request_id").equals(uid)) {
                                request = request_i;
                                requestsFound++;
                            }
                        }

                        if (requestsFound > 1)
                            System.err.println("[WARN]    Duplicate request_id");
                    }

                    if(request != null && response != null) {
                        Platform.runLater(new ProcessRequestHandler(rc, request, response));
                    } else {

                    }

                } catch (IOException e) {
                    if(connected) {
                        System.err.println("[ERROR]   Error in communication, attempting to reconnect");
                        reconnect();
                    } else {
                        //Sleep for one second before attempting again, to give a chance to reconnect
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException interruptedException) {

                        }
                    }
                }
            } else {
                if(connected) {
                    System.err.println("[ERROR]   Connection to server failed, attempting to reconnect");
                    reconnect();
                } else {
                    try {
                        //Sleep for one second before attempting again, to give a chance to reconnect
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {

                    }
                }
            }
        }
    }

    public boolean isConnected() {
        if(socket == null)
            return false;
        else
            return socket.isConnected();
    }

    /*
     * After attempting to reconnect 5 times,
     */
    public void forceReconnect() {
        reconnectTries = 0;
        reconnect();
    }

    /*
     * Method used to reconnect to the server after a connection failure
     */
    public void reconnect() {
        System.err.print("[WARN]    Connection failed or new connection, attempting to reconnect (" + reconnectTries + ")...");

        connected = false;
        socket = null;

        try {
            //if the socket is still open, close it
            if (socket != null && socket.isConnected())
                socket.close();
        } catch (IOException e) {

        }

        try {
            socket = new Socket(host, port);
            System.err.println(" success");
        } catch (IOException e) {
            System.err.print(" failed.");
            if (reconnectTries < maxReconnectTries) {
                System.err.println(" retrying in 5 seconds");
                Timer timer = new Timer();
                timer.schedule(new ReconnectTask(), 5000);
            } else {
                System.err.println();
                System.err.println("[WARN]    Attempts to reconnect failed, abandoning connection.");
            }

            reconnectTries++;
        }

        if(socket != null && socket.isConnected()) {
            rc.onConnect();
            connected = true;
            reconnectTries = 0;
        }
    }

    /*
     * Wrapper for sendMsg.
     */
    public void sendMap(Map<String,Object> toSend) {
        Object uidObject = toSend.get("request_id");
        if (uidObject != null)
            System.err.println("[WARN]    request_id already set, overwriting");

        toSend.put("request_id", Integer.toString(nextUID++));

        requests.add(toSend);

        String s = JsonParser.mapToString(toSend);

        sendMsg(s);
    }

    /*
     * sendMsg itself is private.
     * all calls to daemon are abstracted into other methods.
     */
    private void sendMsg(String msg) {
        if(socket != null && socket.isConnected()) {
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                if (!msg.endsWith("\n"))
                    msg += "\n";

                out.writeBytes(msg);
            } catch (IOException e) {
                System.err.println("[ERROR]   Connection to server failed!");
            }
        } else {
            System.err.println("[SEVERE]  Could not send message, connection failed");
        }
    }

    public void closeConnection() throws Exception {
        socket.close();
    }

    /*
     * This class is used by the reconnect timer to retry after 5 seconds.
     */
    private class ReconnectTask extends TimerTask {
        public void run() {
            reconnect();
        }
    }

    private class ProcessRequestHandler implements Runnable {
        private Map<String,Object> request;
        private Map<String,Object> response;
        ResponseController rc;

        public ProcessRequestHandler(ResponseController rc, Map<String,Object> request, Map<String,Object> response) {
            this.rc = rc;
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            rc.processResponse(request, response);
        }
    }
}
