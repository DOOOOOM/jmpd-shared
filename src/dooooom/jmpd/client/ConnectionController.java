package dooooom.jmpd.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This controller class is intended to store connection state information for the daemon,
 * as well as handling all daemon calls themselves.  These daemon calls should be presented
 * as public non-static methods that will act according to the state information stored
 * in an instance of this class.
 */
public class ConnectionController {
	private Socket socket;
	
	public ConnectionController(String host, int port) throws Exception {
		socket = new Socket(InetAddress.getByName(host), port);
	}
	
	public DaemonResponse callDaemon(DaemonRequest request) {
		return null;
	}
	
	/*
	 * sendMsg itself is private.
	 * all calls to daemon are abstracted into other methods.
	 */
	public String sendMsg(String msg) throws Exception {
		String response = null;
		BufferedReader is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
		outToServer.writeBytes(msg);
		
		response = is.readLine();
		
		System.out.println("response: " + response);
		return response;
	}
	
	public void closeConnection() throws Exception {
		socket.close();
	}
}
