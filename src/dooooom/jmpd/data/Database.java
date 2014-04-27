package dooooom.jmpd.data;
import dooooom.jmpd.daemon.DaemonMainController;

import javax.json.*;
import javax.json.stream.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Database {
	public final String dbLocation = DaemonMainController.getDatabasePath();
    public static ArrayList<Track> library;
	int nextID = 1;
	public Database(){ }

    public void updateDatabase() {
        FileSystemScanner fss = new FileSystemScanner(dbLocation);
        library = fss.returnTracks();
        //add all Track to database
        for(Track t : library){
            addEntry(t,t.get("id"));
        }
    }

    public static ArrayList<Track> search(ArrayList<Track> tl, String key, String value) {
        ArrayList<Track> result = new ArrayList<Track>();

        for(Track tr : tl)
        {
            if(tr.get(key).equals(value))
                result.add(tr);
        }

        return result;
    }

    public static ArrayList<Track> search(String key, String value) {
        ArrayList<Track> result = new ArrayList<Track>();

        for(Track tr : library)
        {
            if(tr.get(key).equals(value))
                result.add(tr);
        }

        return result;
    }
	
	public void addEntry(Map<String, String> data,String tag){
		/**
		 * addEntry: add tracks info into the json database file
		 * tag - String: This is a searchable string which should be
		 * unique and be used to identify this entry.
		 * data - Map<String,String>: This should contain key/value pairs
		 * of the data to be added to database.
		 */
		JsonObjectBuilder copyInto= null;
		JsonObjectBuilder returnedObject = null;
		JsonObjectBuilder mainBuilder = Json.createObjectBuilder();
		try {
			copyInto = Json.createObjectBuilder();
			FileReader fr = new FileReader(dbLocation);
			JsonReader reader = Json.createReader(fr);
			JsonStructure jsonst =  reader.read();
			switch(jsonst.getValueType()){
			case OBJECT:
				JsonObject obj = (JsonObject) jsonst;
				for(String name: obj.keySet()){
					//nothing is done with the null in first iteration
					//because the first expected value is an Object.
					returnedObject = rewriteJson(copyInto,jsonst,null);
					mainBuilder.add(name, returnedObject.build());
				}

			default:
				System.out.println("Default");
				break;
			}
		} catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (javax.json.stream.JsonParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonObjectBuilder obj = Json.createObjectBuilder();
		JsonObject object = mapToJsonObject(data.keySet().iterator(),obj,data);
		System.out.println(object.toString());
		mainBuilder.add(tag,object);
		try {
			FileWriter fl = new FileWriter(dbLocation);
			JsonWriter jWriter = Json.createWriter(fl);
			jWriter.writeObject(mainBuilder.build());
			fl.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nextID++;

	}

	public JsonObject mapToJsonObject(Iterator<String> keys,JsonObjectBuilder obj,Map <String,String> data){
		/**
		 * Helper function used to parse json
		 */
		if(keys.hasNext()){
			String k = (String)keys.next();
			JsonObjectBuilder ob = obj.add(k,data.get(k));
			return mapToJsonObject(keys,ob,data);
		}else{
			JsonObject jb = obj.build();
			return jb;
		}
	}

	public JsonObjectBuilder rewriteJson(JsonObjectBuilder copyInto,JsonValue tree,String key){
		/**
		 * Helper function used to parse json
		 */
		switch(tree.getValueType()){
		case OBJECT:
			JsonObject obj = (JsonObject) tree;
			for(String name : obj.keySet()){
				copyInto = rewriteJson(copyInto,obj.get(name),name);
			}
			break;
		case STRING:
			JsonString st = (JsonString) tree;
			copyInto.add(key, st.getString());
			break;
		default:
			break;
		}
		return copyInto;

	}

	public ArrayList<String> getTrackList(){
		JsonParser jp = null;
		boolean leadingKEY = false;
		String ldKEY = null;
		ArrayList<String> trackID = new ArrayList<String>();
		try {
			jp = Json.createParser(new FileReader(DaemonMainController.getDatabasePath()));
			while(jp.hasNext()){				
				JsonParser.Event event = jp.next();				
				switch(event){
				case START_OBJECT:
					    while(event.toString() != "END_OBJECT"){
					    	event = jp.next();
					    	switch(event){
					    	case KEY_NAME:
					    		if(!leadingKEY){
					    			trackID.add(jp.getString()) ;
					    			leadingKEY = true;
					    		}			    		
					    		break;
					    	case START_OBJECT:
					    		break;					    	
					    	}
					    }
					break;
				case KEY_NAME:
					trackID.add(jp.getString()) ;
					break;
				case END_OBJECT:
					break;
                default:
                    break;				
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jp.close();		
		return trackID;
	}

	public Map<String,String> getEntry(String find){
		/**
		 * getEntry: Would be used to search for an entry
		 * in the database. if the entry is found it would return
		 * all key/value pair as a String to String Map otherwise
		 * return a null.
		 * 
		 * find - String: this is a searchable String used to 
		 * identify the track entry. (same value as tag in addEntry.)
		 */
		JsonParser jp = null;
		String _key = null;
		String searchKey = null;
		Map <String,String> data = new HashMap<String,String>();
		boolean leadingKEY = false;
		String ldKEY = null;
		try {
			jp = Json.createParser(new FileReader(dbLocation));
			while(jp.hasNext()){				
				JsonParser.Event event = jp.next();				
				switch(event){
				case START_OBJECT:
					    while(event.toString() != "END_OBJECT"){
					    	event = jp.next();
					    	switch(event){
					    	case KEY_NAME:
					    		if(!leadingKEY){
					    			ldKEY = jp.getString();
					    			searchKey = ldKEY;
					    			leadingKEY = true;
					    		}else{
					    			_key = jp.getString().trim();
					    		}				    		
					    		break;
					    	case VALUE_STRING:
					    		if (_key != null){
			        				//match key and value
					    			//System.out.print(event.toString()+ " "+jp.getString()+" \n");
			        				data.put(_key,jp.getString().trim());
			        				//make key null for next key/value pair
			        				_key = null;
			        			}
					    		break;
					    	case START_OBJECT:
					    		break;

					    	}
					    }
					    //data point. Data is ready at this point.
					    //System.out.println("find "+find+" search "+searchKey);
					    if(searchKey.compareTo(find) == 0){
					    	return data;
					    }
					break;
				case KEY_NAME:
					searchKey = jp.getString();
					break;
				case END_OBJECT:
					break;				
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jp.close();
		return null;
	}

    public String removeUnicode(String s) {
        return s.replaceAll("[^\\x20-\\x7e]", "");
    }
}
