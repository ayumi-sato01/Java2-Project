package com.bushnell;

public class Database {
    public static String DBName = "jdbc:sqlite:VR-Factory.db"; 

    public static boolean setDBDirectory(String dbPath) {
        try {
            DBName = "jdbc:sqlite:" + dbPath;
            System.out.println("Setting DB path to: " + DBName);
            return true;
        } catch (Exception e) {
            System.out.println("Failed to set DB path");
            e.printStackTrace();
            return false;
        }
    }
}
