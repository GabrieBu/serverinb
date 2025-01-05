package com.example.serverinb.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class Server{
    private static Map<String, Integer> clientsInfo;
    private final int POOL_SIZE = 16;
    private final int SERVER_PORT = 8189;
    private final ObservableList<String> logMessages;

    public Server(){
        clientsInfo = new HashMap<>();
        this.logMessages = FXCollections.observableArrayList();
    }

    public ObservableList<String> getLogMessages(){
        return logMessages;
    }

    public HashMap<String, Integer> getClientsInfo(){
        return (HashMap<String, Integer>) clientsInfo;
    }

    public boolean hasKey(String key){
        return clientsInfo.get(key) != null;
    }

    public int getPort(String emailAddress){
        return clientsInfo.get(emailAddress);
    }

    public void putPort(String emailAddress, int port){
        clientsInfo.put(emailAddress, port); //replaced value if we try to put multiple instances for a paritcular key (client)
    }

    public void deletePort(String emailAddress){
        clientsInfo.remove(emailAddress);
    }

    public int getPoolSize(){
        return POOL_SIZE;
    }

    public int getServerPort(){
        return SERVER_PORT;
    }
}
