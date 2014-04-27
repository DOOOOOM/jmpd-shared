package dooooom.jmpd.client;

import dooooom.jmpd.data.JsonParser;
import javafx.application.Platform;

import javax.json.stream.JsonParsingException;
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
     * Since sometimes^ the socket.isConnected() returns true even when a connection is failing,
     * after seeing a connection error this will be flagged as false until reconnect() succeeds.
     */
    private boolean connected = false;

    /*
     * rc calls will be made
     */
    ResponseController rc;

    BufferedReader in = null;

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
                if(in == null) {
                    /*
                     * If the connection was recently reset or this is a new connection,
                     * we will need a new BufferedReader.
                     */
                    try {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (IOException e) {
                        System.err.println(System.currentTimeMillis() + " [ERROR]   Unable to initialize BufferedReader");
                    }
                } else {
                    try {
                        String s = in.readLine();

                        Map<String, Object> response;

                        try {
                            response = JsonParser.stringToMap(s);
                        } catch (JsonParsingException e) {
                            System.err.println(System.currentTimeMillis() + " [SEVERE]  Json parsing exception for daemon message: " + s);
                            e.printStackTrace();
                            continue;
                        }

                        String uid = (String) response.get("request_id");

                        Map<String, Object> request = null;

                        if (uid == null) {
                            System.err.println(System.currentTimeMillis() + " [WARN]    Missing request_id in daemon message");
                        } else if (uid.equals("0")) {
                        /*
                         * request_id:0 is a special case reserved for
                         * the server sending unsolicited information to the client
                         */
                            request = new HashMap<String, Object>();
                            request.put("command", "INFO");
                            request.put("request_id", "0");
                        } else {
                            int requestsFound = 0;

                            for (Map<String, Object> request_i : requests) {
                                if (request_i.get("request_id").equals(uid)) {
                                    request = request_i;
                                    requestsFound++;
                                }
                            }

                            if (requestsFound > 1)
                                System.err.println(System.currentTimeMillis() + " [WARN]    Duplicate request_id");
                        }

                        if (request != null && response != null) {
                            Platform.runLater(new ProcessResponseHandler(rc, request, response));
                        } else {
                            System.err.println(System.currentTimeMillis() + " [WARN]    Null object in response handling");
                        }
                    } catch (IOException e) {
                        if (connected) {
                            System.err.println(System.currentTimeMillis() + " [ERROR]   Error in communication, attempting to reconnect");
                            reconnect();
                        } else {
                            //Sleep for some time before attempting again, to give a chance to reconnect
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException interruptedException) {

                            }
                        }
                    }
                }
            } else {
                if(connected) {
                    System.err.println(System.currentTimeMillis() + " [ERROR]   Connection to server failed, attempting to reconnect");
                    reconnect();
                } else {
                    try {
                        //Sleep for some time before attempting again, to give a chance to reconnect
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {

                    }
                }
            }
        }
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

        /*
         *                   I dedicate this method to
         *
         *       ~~~~THE IDIOT THAT WROTE Socket.isConnected()~~~~
         *
         *    For the amazing ability to write poor documentation and
         *    make poor decisions even when faced with the most easily
         *                     accomplished tasks.
         *
         *  Thanks to his/her service to the Java language and community,
         *  I was able to learn a great deal of things about debugging in
         *           Java, and learn a valuable lesson in life.
         *
         *                       In short, RTFM.
         *
         *                Dedication by Philip Rodning
         *         On this Twenty-Seventh in the Month of April,
         *            in the Year Two Thousand and Fourteen
         */

        System.err.println(System.currentTimeMillis() + " [WARN]    Connection failed or new connection, attempting to reconnect (" + reconnectTries + ")...");

        Platform.runLater(new RunRCMethod(rc, RCMethod.ON_DISCONNECT));

        /*
         * Make sure that any other parts of the code that might attempt to reconnect()
         * are aware that this method has already been called.
         */
        connected = false;

        try {
            //if the socket is still open, close it
            if (socket != null && socket.isConnected())
                socket.close();
        } catch (IOException e) {
            
        }

        try {
            socket = new Socket(host, port);
            in = null;
            System.err.println(System.currentTimeMillis() + " [INFO]    Connection successfully established");
        } catch (IOException e) {
            socket = null;
            System.err.print(" failed.");
            if (reconnectTries < maxReconnectTries) {
                System.err.println(" retrying in 5 seconds");
                Timer timer = new Timer();
                timer.schedule(new ReconnectTask(), 5000);
            } else {
                System.err.println();
                System.err.println(System.currentTimeMillis() + " [WARN]    Attempts to reconnect failed, abandoning connection.");
            }

            reconnectTries++;
        }

        if(socket != null && !socket.isClosed()) {
            //Platform.runLater(new RunRCMethod(rc, RCMethod.ON_CONNECT));
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
            System.err.println(System.currentTimeMillis() + " [WARN]    request_id already set, overwriting");

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
                System.err.println(System.currentTimeMillis() + " [ERROR]   Connection to server failed!");
            }
        } else {
            System.err.println(System.currentTimeMillis() + " [SEVERE]  Could not send message, connection failed");
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

    private enum RCMethod {
        ON_CONNECT,
        ON_DISCONNECT
    };

    private class RunRCMethod implements Runnable {
        RCMethod rcMethod;
        ResponseController rc;

        public RunRCMethod(ResponseController rc, RCMethod rcMethod) {
            this.rc = rc;
            this.rcMethod = rcMethod;
        }

        @Override
        public void run() {
            switch (rcMethod) {
                case ON_CONNECT:
                    rc.onConnect();
                    break;
                case ON_DISCONNECT:
                    rc.onDisconnect();
                    break;
            }
        }
    }

    private class ProcessResponseHandler implements Runnable {
        private Map<String,Object> request;
        private Map<String,Object> response;
        ResponseController rc;

        public ProcessResponseHandler(ResponseController rc, Map<String, Object> request, Map<String, Object> response) {
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
