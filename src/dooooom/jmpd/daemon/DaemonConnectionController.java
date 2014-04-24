package dooooom.jmpd.daemon;

import dooooom.jmpd.data.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DaemonConnectionController implements Runnable {
    private ServerSocket listenSocket;

    private int port;
    private RequestController rc;

    private List<Thread> threads = new ArrayList<Thread>();

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

        try {
            while (true) {
                Socket socket = null;
                try {
                    socket = listenSocket.accept();
                    System.err.println("[INFO]    Connection accepted");
                    Thread connectionThread = new Thread(new ConnectionHandler(socket));
                    threads.add(connectionThread);
                    connectionThread.start();
                    socket = null;
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

    private class ConnectionHandler implements Runnable {
        Socket socket;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            while (socket != null && socket.isConnected()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                    String s = in.readLine();

                    Map<String,Object> request = JsonParser.stringToMap(s);
                    Map<String,Object> response = rc.processRequest(request);
                    String responseString = JsonParser.mapToString(response);

                    if (!responseString.endsWith("\n"))
                        responseString += "\n";

                    out.writeBytes(responseString);
                } catch (IOException e) {
                    System.err.println("[ERROR]   Error in communication with client");

                    try {
                        if (socket != null)
                            socket.close();
                    } catch (IOException e0) {
                        System.err.println("[ERROR]   Error in closing socket");
                    }

                    break;
                }
            }
        }
    }
}