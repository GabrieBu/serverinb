package com.example.serverinb.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Server{
    private int POOL_SIZE;
    private int SERVER_PORT;
    private final ObservableList<String> logMessages;

    public Server() throws FileNotFoundException {
        this.logMessages = FXCollections.observableArrayList();

        setConfig();
    }

    private void setConfig() throws FileNotFoundException {
        File configFile = new File("C:\\Users\\andre\\Desktop\\Prog3\\PROGETTO_SERVER\\NEWserver\\serverinb\\src\\main\\java\\com\\example\\serverinb\\Storage\\config.txt");
        try (Scanner scanner = new Scanner(configFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    try {
                        switch (key) {
                            case "POOL_SIZE":
                                this.POOL_SIZE = Integer.parseInt(value);
                                break;
                            case "PORT":
                                this.SERVER_PORT = Integer.parseInt(value);
                                break;
                            default:
                                System.out.println("Unrecognized key: " + key);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error loading config file " + key + ": " + value);
                    }
                }
            }
        }
    }

    public ObservableList<String> getLogMessages(){
        return logMessages;
    }

    public int getPoolSize(){
        return POOL_SIZE;
    }

    public int getServerPort(){
        return SERVER_PORT;
    }
}
