package dooooom.jmpd.daemon;

import dooooom.jmpd.data.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class DaemonConnectionController implements Runnable {
    private ServerSocket listenSocket;

    private int port;
    private RequestController rc;

//    private List<Thread> threads = new ArrayList<Thread>();
//    private List<ConnectionHandler> connectionHandlers = new ArrayList<ConnectionHandler>();
    private Map<ConnectionHandler,Thread> connectionHandlerThreadMap = new HashMap<ConnectionHandler, Thread>();

    public DaemonConnectionController(int port, RequestController rc) {
        this.port = port;
        this.rc = rc;
    }

    @Override
    public void run() {
        try {
            listenSocket = new ServerSocket(port);
            System.err.println("[INFO]    Now listening on port " + port);
        } catch (IOException e) {
            System.err.println("[FATAL]   Server could not bind on port " + port);
            System.exit(1);
        }

        Timer connectionTimer = new Timer();
        connectionTimer.schedule(new RemoveUnusedConnectionsTask(), 0, 100);

        try {
            while (true) {
                Socket socket = null;
                try {
                    socket = listenSocket.accept();
                    System.err.println("[INFO]    Connection accepted from " + socket.getInetAddress() + " at " + System.currentTimeMillis());

                    ConnectionHandler ch = new ConnectionHandler(socket);


                    Thread connectionThread = new Thread(ch);

                    synchronized (connectionHandlerThreadMap) {
                        connectionHandlerThreadMap.put(ch, connectionThread);
                    }
                    connectionThread.start();
                } catch (IOException e) {
                    System.err.println("[ERROR]   Server could not accept connection");
                    System.exit(1);
                }
            }
        } finally {
            try {
                listenSocket.close();
            } catch (IOException e) {
                System.err.println("[ERROR]   Server could not close listening connection");
            }
        }
    }

    public void sendToAll(Map<String,Object> response) {
        for(ConnectionHandler ch : connectionHandlerThreadMap.keySet()) {
            Socket socket = ch.getSocket();

            send(socket, response);
        }
    }

    private void send(Socket socket, Map<String,Object> response) {
        try {
            synchronized (socket) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//                PrintWriter pw = new PrintWriter(out);

                String responseString = JsonParser.mapToString(response);

                if (!responseString.endsWith("\n"))
                    responseString += "\n";

                out.writeBytes(responseString);
//                pw.write(responseString);

//                if(pw.checkError())
//                    System.err.println("[ERROR]   PrintWriter error in send(...) sending: " + responseString);
            }
        } catch (IOException e) {
            System.err.println("[ERROR]   Error in communication with client in send()");

            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e0) {
                System.err.println("[ERROR]   Error in closing socket");
            }
        }
    }

    private class ConnectionHandler implements Runnable {
        private Socket socket;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            BufferedReader in;

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.err.println("[ERROR]   Error in creating new BufferedReader");

                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e0) {
                    System.err.println("[ERROR]   Error in closing socket");
                }

                return;
            }

            while (socket != null && !socket.isClosed()) {
                try {
                    String s = in.readLine();

                    Map<String,Object> request = JsonParser.stringToMap(s);
                    Map<String,Object> response = rc.processRequest(request);

                    if(response == null)
                        continue;

                    if(response.containsKey("DCC_SEND_MULTIPLE")) {
                        List<Map<String,Object>> responses = (List<Map<String,Object>>) response.get("DCC_SEND_MULTIPLE");

                        for(Map<String,Object> sub_response : responses) {
                            send(socket,sub_response);
                        }
                    } else {
                        send(socket, response);
                    }
                }  catch (IOException e) {
                    System.err.println("[ERROR]   Error in communication with client in ConnectionHandler");
                    e.printStackTrace();

                    try {
                        if (socket != null)
                            socket.close();
                    } catch (IOException e0) {
                        System.err.println("[ERROR]   Error in closing socket");
                    }

                    return;
                }
            }
        }

        public Socket getSocket() {
            return socket;
        }
    }

    private class RemoveUnusedConnectionsTask extends TimerTask {
        @Override
        public void run() {
            synchronized (connectionHandlerThreadMap) {
                for (ConnectionHandler ch : connectionHandlerThreadMap.keySet()) {
                    Thread th = connectionHandlerThreadMap.get(ch);

                    if (th.getState() == Thread.State.TERMINATED) {
                        System.err.println("[INFO]    Removing dead ConnectionHandler thread");
                        connectionHandlerThreadMap.remove(ch);
                    }
                }
            }
        }
    }
}