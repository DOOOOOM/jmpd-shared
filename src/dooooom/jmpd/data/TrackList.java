package dooooom.jmpd.data;

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

public class TrackList extends ArrayList<Track> 
{
	public final String dbLocation = new FileSystemScanner().getFolderPath() + new FileSystemScanner().getS() + "database";
	int nextID = 1;

	public TrackList(ArrayList<Track> list)
	{
		for(Track t : list)
		{
			this.add(t);
		}
	}

	public TrackList()
	{
		
	}

	public void updateNextID()
	{
		for(Track track : this)
			if (Integer.parseInt(track.get("id")) > nextID)
				nextID = Integer.parseInt(track.get("id"));
		nextID++;
		//nextID = Integer.parseInt( this.get(new Integer(this.size()).toString()) . get("id") );
	}

	public void loadDatabase()
	{
		ArrayList<String> getID = new ArrayList<String>();
		
		TrackList Database = new FileSystemScanner().returnTracks();
		for (Track t : Database)
		{
			getID.add(t.get("id"));
		}
		
		//ArrayList<String> getID = new FileSystemScanner.returnTracks();
		for(int i = 0; i < getID.size(); i++){
			Map<String,String> result = getEntry(getID.get(i));
			Track newTrack = new Track();
			for(Map.Entry<String, String> entry : result.entrySet()){
				//make a track
				newTrack.put(entry.getKey(), result.get(entry.getKey()));
			}
			this.add(newTrack);
		}

		updateNextID();
	}

	public TrackList search(String key, String value) 
	{
		TrackList result = new TrackList();

		for(Track tr : this) 
		{
			if(tr.get(key).equals(value))
				result.add(tr);
		}

		return result;
	}

	public boolean trackIDExists(Track t) 
	{
		if(this.search("id", t.get("id")).size() > 0)
			return true;
		else
			return false;
	}

	public static void main(String[] args)
	{
		TrackList t = new TrackList();
		t.loadDatabase();
		for (Track tr : t)
			System.out.println(tr.get("id") + " " + tr.get("title") + " " + tr.get("artist") + " " + tr.get("album"));
		System.out.print("The next ID is :" + t.nextID);
	}

	/**
	 * Database Json functions
	 */
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
}
