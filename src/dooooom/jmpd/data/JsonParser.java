package dooooom.jmpd.data;


import javax.json.*;
import java.io.StringReader;
import java.util.*;

public class JsonParser {
    public static void main(String[] args) {
        //testcase one
		Map<String,Object> data = new HashMap<String,Object>();
		List<String> alist = Arrays.asList("firstt","tests","plc","kinda late");
		data.put("trackID", "1");
		data.put("ADD", alist);
		String d1 = mapToString(data);
        System.out.println(data.toString());
		System.out.println(d1);
        Map<String,Object> sm2 = new HashMap<String,Object>();
        sm2 = stringToMap(d1);
        System.out.println(sm2.toString());
        //testcase two
        Map<String,Object> data2 = new HashMap<String,Object>();
        List<Map<String,Object>> anotherList = new ArrayList<Map<String,Object>>();
        data2.put("requestID", "2");
        Map<String,Object> pairs = new HashMap<String,Object>();
        pairs.put("some\"data","o\\ther\"data");
        pairs.put("s\\omeeesta","ot\"herds\\sa");
        pairs.put("somedwwa","othsddata");
        anotherList.add(pairs);
        Map<String,Object> pair = new HashMap<String,Object>();
        pair.put("somedata","otherdata");
        pair.put("someeesta","otherdssa");
        pair.put("somedwwa","othsddata");
        anotherList.add(pair);
        data2.put("ADD",anotherList);
        System.out.println(data2.toString());
        String d = mapToString(data2);
        System.out.println(d.toString());
        Map<String,Object> sm = new HashMap<String,Object>();
        sm = stringToMap(d);
        System.out.println(sm.toString());

    }

    public static String mapToString(Map<String,Object> toSend) {
        String key = null;
        Object object = null;
        JsonObjectBuilder dataContainer = Json.createObjectBuilder();
        for(Map.Entry<String,Object> entry: toSend.entrySet()){
            key = entry.getKey();
            object = toSend.get(entry.getKey());
            if( object instanceof String ){
                dataContainer = dataContainer.add(key, (String)object);
            }else if(object instanceof List){
                //start array object
                JsonArrayBuilder arrayObj = Json.createArrayBuilder();
                List items = (List) object;
                for(Object ob : items){
                    if(ob instanceof Map){
                        //start object
                        JsonObjectBuilder insiderArray = Json.createObjectBuilder();
                        JsonObject mapped = mapToJsonObject(((Map) ob).keySet().iterator(),insiderArray,(Map)ob);
                        arrayObj.add(mapped);
                    }else{
                        //String.
                        arrayObj = arrayObj.add((String) ob);
                    }

                }
                dataContainer.add(key, arrayObj);
            }else{
//                System.err.println("[WARN]   Unrecognized object (" + object.getClass() + ") in map in mapToString(...), using .toString()");
                dataContainer = dataContainer.add(key, object.toString());
            }

        }

        return dataContainer.build().toString();
    }

    public static Map<String,Object> stringToMap(String inComing){
        Map<String,Object> result = new HashMap<String,Object>();

        if(inComing == null) {
            return null;
        }

        JsonReader jsonReader = Json.createReader(new StringReader(inComing));
        JsonObject object = jsonReader.readObject();

        //iterate through map entries
        for(Map.Entry<String,JsonValue> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonValue objV = entry.getValue();

            //determine if entry is a: Map, Array, or String
            switch(objV.getValueType()) {
                case OBJECT:
                    //Apparently this is bad?
                    System.out.println("got object");
                    break;
                case ARRAY:
                    List<Object> listObject = new ArrayList<Object>();
                    JsonArray array = (JsonArray) objV;

                    //iterate through items in array
                    for(JsonValue val : array) {
                        switch(val.getValueType()) {
                            case OBJECT:
                                //found a map, eg. the array is a TrackList and this is a Track
                                Map<String,Object> listMap = new HashMap<String,Object>();
                                JsonObject obj = (JsonObject) val;
                                for(String name : obj.keySet()) {
                                    listMap.put(name, obj.getString(name));
                                }
                                listObject.add(listMap);
                                break;
                            case STRING:
                                JsonString arrayString = (JsonString) val;
                                listObject.add(arrayString.getString());
                                break;
                        }
                    }

                    result.put(key, listObject);
                    break;
                case STRING:
                    JsonString st = (JsonString) objV;
                    result.put(key, st.getString());
                    break;
            }
        }

        return result;
    }

    private static JsonObject mapToJsonObject(Iterator<String> keys,JsonObjectBuilder obj,Map <String,String> data){
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
}