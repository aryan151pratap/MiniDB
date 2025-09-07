package MiniDB.db;

import java.util.ArrayList;
import java.util.Map;

public class finds {
    
    public static boolean matched(Map<String, Object> doc, Map<String, Object> documents) {
        boolean match = true;
        for (String key : doc.keySet()) {
            if (!documents.containsKey(key) || !documents.get(key).equals(doc.get(key))) {
                match = false;
            }
            if(documents.get(key) instanceof Map && doc.get(key) instanceof Map ) {
                match = matched((Map<String, Object>) doc.get(key), (Map<String, Object>) documents.get(key));
            }
            if(!match) break;
        }
        return match;
    }

    public static String keys_values(Map<String, Object> document, String[] keys, int i){
        if (i == keys.length - 1) {
            Object val = document.get(keys[i]);
            return (val != null) ? val.toString() : "null";
        }
        Object next = document.get(keys[i]);
        if (next instanceof Map) {
            return keys_values((Map<String, Object>) next, keys, i + 1);
        }
        return "null";
    }

    public static StringBuilder check_find(ArrayList<Map<String, Object>> docs, Map<String, Object> l, Boolean findOne, Boolean getTime, Boolean findAll, String[] keys ) {
        StringBuilder sb = new StringBuilder();

        ArrayList<Map<String, Object>> all = new ArrayList<>();

        for (Map<String, Object> document : docs) {
            boolean match = true;
            Object id = document.get("_id");
            match = matched(l, document);
            if(keys != null){
                sb.append(id).append(" ");
                for(String key : keys){
                    sb.append(key).append(" -> ").append(keys_values(document, key.split("-"), 0)).append("  ");
                }
                sb.append("\n");
                if(findOne && match) break;
            } else {
                if(findOne && match){
                    sb = display_doc(document, false);
                    break;
                }
                if (findAll && match && id != null) {
                    all.add(document);
                }
            }

            if (getTime && match && id != null) {
                String result = idgenerator.getDate(id);
                sb.append(result).append("\n");
            }
        }
        if (sb.length() == 0) {
            sb.append("No document found");
            return sb;
        }
        if(keys != null) return sb;
        else if (findAll && !all.isEmpty()) {
            sb = display_all(all);
        }

        return sb;
    }

    public static boolean find(String key, Object value, Map<String, Object> document) {
        if (document.containsKey(key)) {
            Object storedValue = document.get(key);
            if (storedValue != null && storedValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static StringBuilder display_all( ArrayList<Map<String, Object>> all) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append("\n");
        for(Map<String, Object> i : all){
            sb.append(display_doc(i, true)).append("\n");
        }
        sb.append("]");
        return sb;
    }

    public static StringBuilder display_doc(Map<String, Object> i, Boolean is_collection){
        StringBuilder sb = new StringBuilder();
        sb.append(is_collection ? "  " : "").append(i).append("\n");
        return sb;
    }

}
