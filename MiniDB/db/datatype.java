package MiniDB.db;

import java.util.Map;
import java.util.TreeMap;

public class datatype {

    public static Object parseValue(String value) {
        value = value.trim();

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        try { return Integer.parseInt(value); } catch (NumberFormatException e) {}
        try { return Double.parseDouble(value); } catch (NumberFormatException e) {}
        if (value.length() == 3 && value.startsWith("'") && value.endsWith("'")) {
            return value.charAt(1);
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    public static String parseKey(String key){
        key = key.trim();
        if (key.startsWith("\"") && key.endsWith("\"")) {
            return key.substring(1, key.length() - 1).trim();
        }
        return key;
    }

    public static Map<String, Object> parse(String s) {
        Map<String, Object> map = new TreeMap<>();
        int i = 0;
        StringBuilder key = new StringBuilder(), val = new StringBuilder();
        boolean inKey = true;

        while (i < s.length()) {
            char c = s.charAt(i);

            if (c == '{') {
                int start = i + 1, cnt = 1;
                while (cnt != 0) {
                    i++;
                    if (s.charAt(i) == '{') cnt++;
                    else if (s.charAt(i) == '}') cnt--;
                }
                map.put(parseKey(key.toString()), parse(s.substring(start, i)));
                key.setLength(0); 
                val.setLength(0); 
                inKey = true;
            }
            else if (c == ':') {
                inKey = false;
            }
            else if (c == ',') {
                if (val.length() > 0) {
                    map.put(parseKey(key.toString()), parseValue(val.toString()));
                }
                key.setLength(0); val.setLength(0); inKey = true;
            }
            else {
                (inKey ? key : val).append(c);
            }
            i++;
        }

        if (key.length() > 0 && val.length() > 0) {
            map.put(parseKey(key.toString()), parseValue(val.toString()));
        }

        return map;
    }
}
