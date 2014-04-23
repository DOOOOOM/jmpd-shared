package dooooom.jmpd.tcptest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {
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
        pairs.put("somedata","otherdata");
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
        String d = mapToString(data2);
        System.out.println(d);
        //Map<String,Object> sm = new HashMap<String,Object>();
        //JParser_mod.mapContainer.clear();
        //sm = stringToMap(d);
        //System.out.println(sm.toString());

    }

    public static Map<String,Object> stringToMap(String input) {
        List<String> allMatches = new ArrayList<String>();

        Matcher m = Pattern.compile("\"[^\"]*\":\"[^\"]*\"").matcher(input);
        while (m.find()) {
            allMatches.add(m.group());
        }

        Map<String,Object> result = new HashMap<String,Object>();

        for (String s : allMatches) {
            String key = s.substring(1,s.indexOf(':') - 1);
            String value = s.substring(s.indexOf(':') + 2, s.length() - 1);

            result.put(key,value);
        }

        return result;
    }

    public static String mapToString(Map<String, Object> input) {
        String result = "{";

        boolean firstelement = true;

        for(String key : input.keySet()) {
            if (firstelement)
                firstelement = false;
            else
                result += ",";

            result += "\"" + key + "\"";
            result += ":";

            Object o = input.get(key);
            String s;

            if(o instanceof List) {
                s = listToString((List)o);
            } else if (o instanceof Map) {
                s = mapToString((Map)o);
            } else if (o instanceof String) {
                s = "\"" + (String)o + "\"";
            } else {
                s = o.toString();
            }
            result += s;
        }

        result += "}";

        return result;
    }

    private static String listToString(List<Object> input) {
        String result = "[";

        boolean firstelement = true;

        for(Object o : input) {
            if (firstelement)
                firstelement = false;
            else
                result += ",";

            String s;

            if(o instanceof List) {
                s = listToString((List)o);
            } else if (o instanceof Map) {
                s = mapToString((Map)o);
            } else if (o instanceof String) {
                s = "\"" + (String)o + "\"";
            } else {
                s = "\"" + o.toString() + "\"";
            }

            result += s;
        }

        result += "]";
        return result;
    }
}
