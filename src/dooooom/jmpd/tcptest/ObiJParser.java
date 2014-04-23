package dooooom.jmpd.tcptest;

import javax.json.*;
import java.util.*;

public class ObiJParser {
    public static Map<String,Object> mapContainer = new HashMap<String,Object>();
    static List<Object> stlst = new ArrayList<Object>();
    static Map<String,Object> mapit = new HashMap<String,Object>();

    public static void main(String[] args) {
        //testcase one
		/*Map<String,Object> data = new HashMap<String,Object>();
		List<String> alist = Arrays.asList("firstt","tests","plc","kinda late");
		data.put("trackID", "1");
		data.put("ADD", alist);
		JsonObject d = mapToString(data);
		System.out.println(d.toString());*/
        //testcase two
        Map<String,Object> data2 = new HashMap<String,Object>();
        List<Map<String,Object>> anotherList = new ArrayList<Map<String,Object>>();
        data2.put("requestID", "2");
        Map<String,Object> pairs = new HashMap<String,Object>();
        pairs.put("some\"data","otherdata");
        pairs.put("someeesta","otherdssa");
        pairs.put("somedwwa","othsddata");
        anotherList.add(pairs);
        Map<String,Object> pair = new HashMap<String,Object>();
        pair.put("somedata","otherdata");
        pair.put("someeesta","otherdssa");
        pair.put("somedwwa","othsddata");
        anotherList.add(pair);
        data2.put("ADD",anotherList);
        System.out.println(data2.toString());
        JsonObject d = mapToString(data2);
        System.out.println(d.toString());
        Map<String,Object> sm = new HashMap<String,Object>();
        //mapContainer.clear();
        sm = stringToMap2(d);
        System.out.println(sm.toString());

    }


    public static JsonObject mapToJsonObject(Iterator<String> keys,JsonObjectBuilder obj,Map <String,String> data){
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

    public static JsonObject mapToString(Map<String,Object> toSend) {
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
                System.out.println("illegal object nested in Map returning... null");
                return null;
            }

        }

        return dataContainer.build();
    }

    public static Map<String,Object> stringToMap2(JsonObject object){
        JsonValue objV = null;
        String key = null;
        Map<String,Object> mapContainer = new HashMap<String,Object>();
        for(Map.Entry<String, JsonValue> entry: object.entrySet()){
            key = entry.getKey();
            objV = entry.getValue();
            switch(objV.getValueType()){
                case OBJECT:
                    System.out.println("got object");
                    break;
                case ARRAY:
                    List<Object> lstObject = new ArrayList<Object>();
                    JsonArray array = (JsonArray) objV;
                    for(JsonValue val : array){
                        switch(val.getValueType()){
                            case OBJECT:
                                Map<String,Object> lstMaps = new HashMap<String,Object>();
                                JsonObject obj = (JsonObject) val;
                                for(String name : obj.keySet()){
                                    lstMaps.put(name, obj.get(name));
                                }
                                lstObject.add(lstMaps);
                                break;
                            case STRING:
                                JsonString arrayString = (JsonString) val;
                                lstObject.add(arrayString.getString());
                                break;
                        }
                    }
                    mapContainer.put(key,lstObject);
                    break;
                case STRING:
                    JsonString st = (JsonString) objV;
                    mapContainer.put(key, st.getString());
                    break;
            }

        }

        return mapContainer;
    }

}