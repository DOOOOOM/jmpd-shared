package dooooom.jmpd.client;
import dooooom.jmpd.client.gui.javafx.MainViewController;
import dooooom.jmpd.data.Command;
import dooooom.jmpd.data.JParser;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class UDPClient implements Runnable {
    final static int messageLength = 1024;
    static DatagramSocket clientSocket;
    InetAddress IPAddress;
    DataOutputStream outToServer;
    DataInputStream inFromServer;
    JParser jsonParser;
    private MainViewController mvc;

    public UDPClient(MainViewController m) {
        mvc = m;
    }

    public void run() {
        final int port = 5006;
        try {
            clientSocket = new DatagramSocket(port);
            System.out.println("Now listening on "+String.valueOf(port));
            //start function to receive responses from server.
            Thread threadListener = new Thread( new Runnable(){
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        listener(port);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            });
            threadListener.start();
            Scanner in = new Scanner(System.in);
            try {
                while (true) {
                    String input = in.nextLine();
                    System.out.println(input);
                    if (input.equals("t")) {
                        sendMessage(Command.TOGGLE, "");
                    } else if (input.equals("n")) {
                        sendMessage(Command.NEXT, "");
                    } else if (input.equals("p")) {
                        sendMessage(Command.PREV, "");
                    } else if (input.equals("s")) {
                        sendMessage(Command.STOP, "");
                    } else if (input.equals("a")) {
                        sendMessage(Command.ADD, "");
                    }
                }
            } catch (NoSuchElementException e) {
                in.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void listener(int port) throws Exception{
        byte[] receiveData = new byte[messageLength];
        while(true){
            //Receiving
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
            //Waits for an incoming datagram.
            clientSocket.receive(receivePacket);
            System.out.println("package received from server");
            //Handle connection request	& Init the thread to start processing request
            Thread thread = new Thread(this.new daemonRequest(clientSocket,receivePacket));
            //start thread
            thread.start();
        }
    }

    public void sendMessage(Command cmd,String arg) throws Exception{
        int port = 5005;
        byte[] envelope;
        ByteArrayOutputStream outGoing = new ByteArrayOutputStream();
        //DatagramSocket socket = new DatagramSocket(5007);
        InetAddress IPAddress = InetAddress.getByName("localhost");
        JsonGenerator jsonGen = Json.createGenerator(outGoing);
        jsonGen.writeStartObject()
                .write("cmd", cmd.toString())
                .writeEnd();
        jsonGen.close();
        //convert ByteStream .. to byte array to send packet
        envelope = outGoing.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(envelope,envelope.length,IPAddress,port);
        clientSocket.send(sendPacket);
    }

    public void sendMessageWithArgList(Command cmd, List<String> result) throws Exception{
        int port = 5005;
        byte[] envelope;
        int PORT = Configure();
        ByteArrayOutputStream outGoing = new ByteArrayOutputStream();
        JsonGenerator jsonGen = Json.createGenerator(outGoing);
        DatagramSocket socket = new DatagramSocket(5007);
        InetAddress IPAddress = InetAddress.getByName("localhost");
        Integer count = new Integer(0);
        JsonGenerator jarray = jsonGen.writeStartObject().writeStartArray(cmd.toString()).writeStartObject();
        for(String eachItem : result){
            count++;
            jarray.write(cmd+"_"+count.toString(),eachItem);
        }
        jarray.writeEnd().writeEnd().writeEnd();
        jsonGen.close();
        //convert ByteStream .. to byte array to send packet
        envelope = outGoing.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(envelope,envelope.length,IPAddress,port);
        clientSocket.send(sendPacket);
    }


    private final class daemonRequest implements Runnable {
        public JParser jsonParser;
        private Map <String,Object> _requestContainer = new HashMap<String,Object>();
        DatagramSocket socket;

        public daemonRequest(DatagramSocket socket,DatagramPacket packet) throws Exception {
            this.socket = socket;
            jsonParser = new JParser(this.socket,packet);
        }

        public void run() {
            try {
                _requestContainer = jsonParser.jsonParser().get(0);
                System.out.println(_requestContainer.toString());

            } catch(Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
    }

    private static int Configure() {
        int port = 5005;
        Properties props = new Properties();
        InputStream in = null;

        try {
            in = new FileInputStream("jmpd.properties");
            if(in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(props.getProperty("Port") != null) {
            port = Integer.parseInt(props.getProperty("Port"));
        }
        System.out.println(port);
        return port;
    }

    public void getPlaylist() throws Exception{
        System.out.println("about to send");
        jsonParser.sendMessage(Command.ADDTOPLAYLIST, "soul");
        if(clientSocket.isClosed()) {
            System.out.println("clientSocket is already closed at this point");
        }
        ArrayList<Map<String, Object>> result = jsonParser.jsonParser();
        //System.out.println(result.toString());
    }
}