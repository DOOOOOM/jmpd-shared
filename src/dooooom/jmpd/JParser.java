//package dooooom.jmpd;

//import dooooom.jmpd.UDPServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

public class JParser {
	DatagramSocket socket;
	ByteArrayOutputStream outGoing;
	ByteArrayInputStream inComing;
	InetAddress IPAddress;
	int port;
	byte[] envelope;
	private Map <String,Object> _responseContainer = new HashMap<String,Object>();
	private Map <String,Object> _requestContainer = new HashMap<String,Object>();
	public JParser(DatagramSocket socket,DatagramPacket receivePacket) throws Exception{
		this.socket = socket;
		outGoing = new ByteArrayOutputStream();
		IPAddress = receivePacket.getAddress();
		port = receivePacket.getPort();
		inComing = new ByteArrayInputStream(receivePacket.getData());
		
	}
	
	public void sendMessage(UDPServer.Command cmd,String arg) throws Exception{
		JsonGenerator jsonGen = Json.createGenerator(outGoing);
		jsonGen.writeStartObject()
			.write(cmd.toString(),arg)
		.writeEnd();
		jsonGen.close();
		//convert ByteStream .. to byte array to send packet
		envelope = outGoing.toByteArray();
		System.out.println("Sending "+envelope.toString()+" to port "+String.valueOf(port));
		DatagramPacket sendPacket = new DatagramPacket(envelope,envelope.length,IPAddress,port);
		this.socket.send(sendPacket);
	}
	
	public void sendMessageWithArgList(UDPServer.Command cmd, List<String> result) throws Exception{
		JsonGenerator jsonGen = Json.createGenerator(outGoing);
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
		this.socket.send(sendPacket);
	}
	
	public ArrayList<Map> jsonParser ()throws Exception{
		String _key = null;
    	JsonParser jParser = Json.createParser(inComing);
    	Map<String,Object> record = new HashMap<String,Object>();
    	ArrayList<Map>dataContainer = new ArrayList<Map>();
    	try{
    		while(jParser.hasNext()){
        		JsonParser.Event event = jParser.next();
        		switch(event){
        		case KEY_NAME:
        			System.out.print(event.toString()+ " "+jParser.getString()+" ");
        			 //save to key/cmd
        			_key = jParser.getString();
        			break;
        		case VALUE_STRING:
        			System.out.println(event.toString()+ " "+jParser.getString()+" ");
        			if (_key != null){
        				record.put(_key, jParser.getString());
        				//_responseContainer.put(_key, jParser.getString());
        				//make key null for next key/value pair
        				_key = null;
        			}
        			break;
        		case VALUE_NUMBER:
        			System.out.println(event.toString()+ " "+jParser.getString()+" ");
        			if (_key != null){
        				//match key and value
        				_responseContainer.put(_key, jParser.getString());
        				//make key null for next key/value pair
        				_key = null;
        			}
        			break;
        		case START_ARRAY:
        			java.util.List<String> options = new java.util.ArrayList<String>();
        			while(event.toString() != "END_ARRAY"){
        				if (event.toString() == "KEY_NAME"){
        				    System.out.print("\t"+event.toString()+ " "+jParser.getString());
        				    //ignore KEY_NAME for arrays.
        				}else if(event.toString() == "VALUE_STRING"){
        					System.out.println(event.toString()+ " "+jParser.getString()+" ");
        					if(_key != null){
        						options.add(jParser.getString());
        					}
        				}else if(event.toString() == "VALUE_NUMBER"){
        					System.out.println(event.toString()+ " "+jParser.getString()+" ");
        					if(_key != null){
        						options.add(jParser.getString());
        					}
        				}else{
        					if (event.toString() != "START_OBJECT"){
        						System.out.println(event.toString());
        					}else if (event.toString() != "END_OBJECT"){
        						System.out.println(event.toString());
        					}else if (event.toString() != "START_ARRAY"){
        						System.out.println(" "+event.toString());
        					}else if (event.toString() != "END_ARRAY"){
        						System.out.println(event.toString());
        					}else{
        						System.out.println(event.toString());
        					}        					
        				}
        				event = jParser.next();      				
        			}
        			if(_key != null){
        				record.put(_key, jParser.getString());
        				//_responseContainer.put(_key, options);
        				_key = null;
        			}
        			break;
        		case START_OBJECT:
        			record = new HashMap<String,Object>();
        			break;
        		case END_OBJECT:
        			if(record != null){
        				dataContainer.add(record);
        			}
        			record = null;
        			break;
        		case END_ARRAY:
        		case VALUE_TRUE:
        		case VALUE_FALSE:
        		case VALUE_NULL:
        			break;
        		
        		}
        		
        	}
    		
    	}catch(JsonParsingException e){
    		System.out.println(e);    		
    	}
    	
    	//return _responseContainer; 
    	return dataContainer;
    }

}
