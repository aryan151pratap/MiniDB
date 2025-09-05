package MiniDB.db;

import java.util.*;

import MiniDB.save.saveData;

public class Crud {
    static Map<String, Map<String, ArrayList<Map<String, Object>>>> db = new TreeMap<>();
    static String current_db = null;

    static {
        saveData.loadDatabaseNames(db);
    }
	
    private static boolean check_status() {
        if (current_db == null) {
            return false;
        }
        return true;
    }

    private static Map<String, Object> command_list(String command) {
        try {
            String l = command.split("\\(")[1].split("\\)")[0].trim();

            if (l.startsWith("{") && l.endsWith("}")) {
                l = l.substring(1, l.length() - 1);
            }
            return datatype.parse(l);

        } catch (Exception e) {
            return null;
        }
    }


    public static String show_data() {
        if (current_db == null || current_db.isEmpty()) return "No database selected. Use 'use <dbname>' first.";
        if (!db.containsKey(current_db)) return "Database '" + current_db + "' not found in memory!";
        Map<String, ArrayList<Map<String, Object>>> collections = db.get(current_db);
        if (collections == null || collections.isEmpty()) return "No collections found in database: " + current_db;
        StringBuilder sb = new StringBuilder();
        sb.append("Database (").append(current_db.toUpperCase()).append(") \n\n");

        for (Map.Entry<String, ArrayList<Map<String, Object>>> entry : collections.entrySet()) {
            sb.append("Collection: ").append(entry.getKey()).append("\n");

            ArrayList<Map<String, Object>> documents = entry.getValue();
            if (documents.isEmpty()) {
                sb.append("  (no documents)\n");
            } else {
                for (int i = 0; i < documents.size(); i++) {
                    sb.append("  Doc ").append(i + 1).append(": ")
                    .append(documents.get(i).toString())
                    .append("\n\n");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

	
	public static String use_db(String command) {
		String[] parts = command.split(" ");
		if (parts.length < 2) return "Error: Database name missing!";
		String _db = parts[1];
		if (!db.containsKey(_db)) return "Error: Database does not exist!";
		current_db = _db;
        return saveData.loadDatabase(current_db, db);
	}

	public static String show_db() {
		if (db.isEmpty()) return "No databases found!";
		StringBuilder sb = new StringBuilder();
		sb.append("Databases:\n");
		for (String dbName : db.keySet()) {
			if (dbName.equals(current_db)) {
				sb.append("* ").append(dbName).append("\n");
			} else {
				sb.append("- ").append(dbName).append("\n");
			}
		}
		return sb.toString();
	}

	public static String getCollections() {
		if (current_db == null || current_db.isEmpty()) return "No database selected. Use 'use <dbname>' first.";

		Map<String, ArrayList<Map<String, Object>>> collections = db.get(current_db);
		if (collections == null || collections.isEmpty()) return "No collections found in database: " + current_db;

		StringBuilder sb = new StringBuilder();
		sb.append("Collections in ").append(current_db).append(":\n");
		for (String collectionName : collections.keySet()) {
			sb.append("- ").append(collectionName).append("\n");
		}

		return sb.toString();
	}


    public static String make_db(String command) {
        String db_name = command.split("\\(")[1].split("\\)")[0];
        if (!db.containsKey(db_name)){
            db.put(db_name, new TreeMap<>());
        }
        return saveData.saveDatabase(db_name, db);
    }

    public static String make_collection(String command) {
        String collection = command.split("\\(")[1].split("\\)")[0];
        if (!check_status()) return "Select Database!";
    
        Map<String, ArrayList<Map<String, Object>>> collections = db.get(current_db);
        if (!collections.containsKey(collection)) {
            collections.put(collection, new ArrayList<>());
        }
        return saveData.saveCollection(current_db, collection, db);
    }

    public static String make_document(String command, boolean insertAll) {
        if (!check_status()) return "Select Database!";
        String coll = command.split("\\.")[1];

        if (!db.containsKey(current_db)) {
            return "Database '" + current_db + "' does not exist!";
        }
        Map<String, ArrayList<Map<String, Object>>> collections = db.get(current_db);
        if (!collections.containsKey(coll)) {
            return "Collection '" + coll + "' does not exist!";
        }

        Map<String, Object> newDoc = command_list(command);
        String _id = idgenerator.generateId();
        newDoc.put("_id", _id);
        collections.get(coll).add(newDoc);
        return saveData.saveCollection(current_db, coll, db);
    }

	public static String delete_document(String command) {
		if (!check_status()) return "Select Database!";
		try {
			String coll = command.split("\\.")[1];
			Map<String, Object> doc = command_list(command);
			Map<String, ArrayList<Map<String, Object>>> collections = db.get(current_db);
			ArrayList<Map<String, Object>> documents = collections.get(coll);
			if (documents == null) return "Collection '" + coll + "' not found!";

            boolean deleted = false;
            Iterator<Map<String, Object>> it = documents.iterator();
            while (it.hasNext()) {
                Map<String, Object> i = it.next();
                if (finds.matched(doc, i)) {
                    it.remove();
                    deleted = true;
                }
            }

            if (deleted) {
                saveData.saveCollection(current_db, coll, db);
                return "Document(s) deleted";
            } else {
                return "No matching document found to delete!";
            }
		} catch (Exception e) {
			return "Error parsing command!" + e.getMessage();
		}
	}

    public static String findOne(String command, String active){
        try{
            String[] l = command.split("\\.");
            String coll = l[1];
			Map<String, Object> doc = command_list(command);
            String[] keys = null;
            if(command.contains("valueof") && l.length == 4){
                keys = l[l.length - 1].split("\\{")[1].split("\\}")[0].trim().split(",");
            }
            if(!db.get(current_db).containsKey(coll)){
                return "Collection " + coll + " not exists 💦";
            }
            ArrayList<Map<String, Object>> docs = db.get(current_db).get(coll);
            StringBuilder sb = new StringBuilder();

            if(docs.isEmpty()) return "No document found in Collection " + coll;

            if(doc == null || doc.isEmpty()){
                if(keys != null){
                    for(Map<String, Object> document : docs){
                        sb.append(document.get("_id")).append(" ");
                        for(String key : keys){
                            key = key.trim();
                            sb.append(key).append(" -> ").append(finds.keys_values(document, key.split("-"), 0)).append("  ");
                        }
                        sb.append("\n");
                        if(active.equals("findOne")) break;
                    }
                } else {
                    if(active.equals("findOne")){
                        sb = finds.display_doc(docs.get(0), false);
                    } else if(active.equals("findAll")){
                        sb = finds.display_all(docs);
                    }
                } 
            }
            else{
                System.out.println("keys " + keys + " doc " + doc);
                if(active.equals("findOne")){
                    sb = finds.check_find(docs, doc, true, false, false, keys);
                } else if(active.equals("findAll")){
                    sb = finds.check_find(docs, doc, false, false, true, keys);
                } else if(active.equals("getTime")){
                    sb = finds.check_find(docs, doc, false, true, false, keys);
                }
            }
            if (sb.length() == 0) {
				return "no document found to get time!";
			}
            return sb.toString();
        } catch (Exception e) {
			return "Error: parsing command!";
		}
    }

    public static String update_doc(String command){
        try{
            String[] whole_command = command.split("\\.");
            String coll = whole_command[1];
            Map<String, Object> find = command_list(whole_command[2]);
			Map<String, Object> set = command_list(whole_command[whole_command.length - 1]);

            ArrayList<Map<String, Object>> docs = db.get(current_db).get(coll);
            StringBuilder sb = new StringBuilder();

            boolean updated = false;
            
            for (Map<String, Object> document : docs) {
                boolean matched = finds.matched(find, document);
                if (matched) {
                    set_update_value(document, set);
                    updated = true;
                    sb.append("✅ Updated: ").append(document).append("\n");
                }
            }

            if (!updated) return "No document matched containing '" + find + "'";
            saveData.saveCollection(current_db, coll, db);
            return sb.toString();
        } catch (Exception e) {
			return "Error parsing command!";
		}
    }

    public static void set_update_value(Map<String, Object> document, Map<String, Object> set) {
        for (String key : set.keySet()) {
            Object newVal = set.get(key);

            if (newVal instanceof Map && document.get(key) instanceof Map) {
                set_update_value((Map<String, Object>) document.get(key), (Map<String, Object>) newVal);
            } else {
                document.put(key, newVal);
            }
        }
    }


    public static String drop_database(String command){
        try{
            String db_name = command.split(" ")[1];
            db.remove(db_name);
            return saveData.Drop_db(db_name);
        } catch (Exception e) {
			return "Error parsing command!";
		}
    }

    public static String drop_collection(String command){
        try{
            String[] l = command.split(" ");
            String collection = l[l.length - 1];
            if(!db.get(current_db).containsKey(collection)) return "collection " + collection + " not exists";
            db.get(current_db).remove(collection);
            saveData.Drop_collection(current_db, collection, db);
            return "🗑️ collection " + collection + " remove";
        } catch (Exception e) {
			return "Error parsing command!";
		}
    }

}
