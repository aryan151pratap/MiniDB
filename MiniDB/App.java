package MiniDB;

import java.util.Scanner;

import MiniDB.db.Crud;

public class App {
    public static void main(String[] args) {
        boolean exit = false;
        Scanner sc = new Scanner(System.in);

        while (!exit) {
            System.out.print("MiniDB> ");
            String command = sc.nextLine().trim();

            if (command.equalsIgnoreCase("exit")) {
                exit = true;
            } else if (command.startsWith("db.create(")) {
                System.out.println(Crud.make_db(command));
            } else if (command.startsWith("use")) {
                Crud.use_db(command);
            } else if (command.equalsIgnoreCase("show")) {
                Crud.show_db();
            } else if (command.startsWith("db.createcollection(")) {
                System.out.println(Crud.make_collection(command));
            } else if (command.startsWith("db") && command.contains("insert")) {
                System.out.println(Crud.make_document(command, false));
            } else if (command.startsWith("db") && command.contains("getTime")) {
                System.out.println(Crud.findOne(command, "getTime"));
            }
        }

        sc.close();
    }

	public static String execute_Command(String command) {
        if (command == null || command.isEmpty()) return "";

        switch (command.toLowerCase()) {
            case "help":
                return "Commands: help, version, exit";
            case "version":
                return "MiniDB Console Version 1.0";
            case "exit":
                return "Exiting MiniDB...";
        }

        try {
            if (command.startsWith("db.create(")) {
                return Crud.make_db(command);
            } else if (command.startsWith("use")) {
                return Crud.use_db(command);
            } else if (command.equalsIgnoreCase("show")) {
                return Crud.show_data();
            } else if (command.equalsIgnoreCase("show databases")) {
                return Crud.show_db();
            } else if (command.equalsIgnoreCase("show collections")) {
                return Crud.getCollections();
            } else if (command.startsWith("db.createcollection(")) {
                return Crud.make_collection(command);
            } else if (command.startsWith("db") && command.contains("findOne")) {
                return Crud.findOne(command, "findOne");
            } else if (command.startsWith("db") && command.contains("getTime")) {
                return Crud.findOne(command, "getTime");
            } else if (command.startsWith("db") && command.contains("findAll")) {
                return Crud.findOne(command, "findAll");
            } else if (command.startsWith("db") && command.contains("insert")) {
                return Crud.make_document(command, false);
            } else if (command.startsWith("db") && command.contains("insertAll")) {
                return Crud.make_document(command, true);
            } else if (command.startsWith("db") && command.contains("deleteDoc")) {
                return Crud.delete_document(command);
            } else if (command.startsWith("db") && command.contains("updateDoc") && command.contains("set")) {
                return Crud.update_doc(command);
            } else if (command.startsWith("drop collection")) {
                return Crud.drop_collection(command);
            } else if (command.startsWith("drop")) {
                return Crud.drop_database(command);
            } else {
                return "Error: Unknown command '" + command + "'";
            }
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
}
