package dooooom.jmpd.tcptest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {
    public static String mapToString(Map<String, String> input) {
        String result = "{";

        boolean firstelement = true;

        for(String key : input.keySet()) {
            if (firstelement)
                firstelement = false;
            else
                result += ",";

            result += "\"" + key + "\"";
            result += ":";
            result += "\"" + input.get(key) + "\"";
        }

        result += "}";

        return result;
    }

    public static Map<String,String> stringToMap(String input) {
        List<String> allMatches = new ArrayList<String>();

        Matcher m = Pattern.compile("\"[^\"]*\":\"[^\"]*\"").matcher(input);
        while (m.find()) {
            allMatches.add(m.group());
        }

        Map<String,String> result = new HashMap<String,String>();

        for (String s : allMatches) {
            String key = s.substring(1,s.indexOf(':') - 1);
            String value = s.substring(s.indexOf(':') + 2, s.length() - 1);

            result.put(key,value);
        }

        return result;
    }
}
