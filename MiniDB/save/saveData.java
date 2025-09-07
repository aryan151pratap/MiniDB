package MiniDB.save;

import java.io.*;
import java.util.*;

public class saveData {

    private static File getDataFolder() {
        File folder = new File(System.getProperty("user.dir"), "data");
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public static String loadDatabaseNames(Map<String, Map<String, ArrayList<Map<String, Object>>>> db) {
        File folder = getDataFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".db"));
        if (files == null || files.length == 0) return "No databases found!";
        for (File file : files) {
            String dbName = file.getName().replace(".db", "");
            db.putIfAbsent(dbName, new TreeMap<>());
        }
        return "Loaded database names into memory: " + db.keySet();
    }

    public static String saveDatabase(String dbName,
            Map<String, Map<String, ArrayList<Map<String, Object>>>> db) {
        try {
            if (!db.containsKey(dbName)) return "Database '" + dbName + "' does not exist in memory!";
            File folder = getDataFolder();
            File file = new File(folder, dbName + ".db");
            if (file.exists()) return "Database '" + dbName + "' already exists on disk!";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(db.get(dbName));
            }
            return "✅ Database '" + dbName + "' saved to " + file.getAbsolutePath();
        } catch (IOException e) {
            return "Error saving database: " + e.getMessage();
        }
    }

    public static String saveCollection(String current_db, String collection,
        Map<String, Map<String, ArrayList<Map<String, Object>>>> db) {
        try {
            if (!db.containsKey(current_db)) return "Database '" + current_db + "' does not exist in memory!";
            Map<String, ArrayList<Map<String, Object>>> collections = db.get(current_db);
            if (!collections.containsKey(collection)) return "Collection '" + collection + "' does not exist in database '" + current_db + "'!";
            File folder = getDataFolder();
            File file = new File(folder, current_db + ".db");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(db.get(current_db));
            }
            return "✅ Collection '" + collection + "' saved in database '" + current_db + "' (" + file.getAbsolutePath() + ")";
        } catch (IOException e) {
            return "Error saving collection: " + e.getMessage();
        }
    }

    public static String Drop_db(String db_name){
        try{
            File folder = getDataFolder();
            File file = new File(folder, db_name + ".db");
            if (file.exists()) {
                if (file.delete()) return "Database '" + db_name + "' dropped and file deleted (" + file.getAbsolutePath() + ")";
                else return "Database '" + db_name + "' removed from memory but failed to delete file!";
            } else {
                return "✅ Database '" + db_name + "' dropped (no file found).";
            }
        } catch (Exception e) {
            return "Error parsing command! " + e.getMessage();
        }
    }

    public static String Drop_collection(String db_name, String collection, Map<String, Map<String, ArrayList<Map<String, Object>>>> db){
        try{
            File folder = getDataFolder();
            File file = new File(folder, db_name + ".db");
            if (file.exists()) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(db.get(db_name));
                }
                return "Database " + db_name + " collection " + collection + " dropped!";
            }
            return "✅ Database collection '" + collection + "' dropped (no file found).";
        } catch (Exception e) {
            return "Error parsing command! " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public static String loadDatabase(String dbName,
            Map<String, Map<String, ArrayList<Map<String, Object>>>> db) {
        try {
            File folder = getDataFolder();
            File file = new File(folder, dbName + ".db");
            if (!file.exists()) return "File for database '" + dbName + "' not found!";
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Map<String, ArrayList<Map<String, Object>>> databaseData =
                        (Map<String, ArrayList<Map<String, Object>>>) ois.readObject();
                db.put(dbName, databaseData);
            }
            return "✅ Database '" + dbName + "' loaded from " + file.getAbsolutePath();
        } catch (IOException | ClassNotFoundException e) {
            return "Error loading database: " + e.getMessage();
        }
    }
}
