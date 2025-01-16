package com.example.serverinb.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Server{
    private final int POOL_SIZE = 16;
    private final int SERVER_PORT = 8189;
    private long currentIdMail;
    private final ObservableList<String> logMessages;

    public Server(){
        this.logMessages = FXCollections.observableArrayList();
        currentIdMail = Long.MIN_VALUE + 1;
    }

    public long getCurrentIdMail() {
        return currentIdMail;
    }

    public void incrementCurrentIdMail(long currentIdMail) {
        this.currentIdMail++;
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
