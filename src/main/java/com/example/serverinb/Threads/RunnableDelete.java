package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.utils.FileManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.IOException;

public class RunnableDelete implements Runnable{
    private final String clientReqString;
    private final Server server;
    private final FileManager fileManager;

    public RunnableDelete(String cientReqString, Server server) {
        this.clientReqString = cientReqString;
        this.server = server;
        fileManager = new FileManager();
    }

    public void run() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        String mailUser = jsonObjectReq.get("user").getAsString();
        int indexToRemove = jsonObjectReq.get("index_to_remove").getAsInt();
        try {
            fileManager.rewriteFile(mailUser, indexToRemove);
        } catch (IOException e) {
            throw new RuntimeException(e); //handle better
        }
        Platform.runLater(() -> {
            server.getLogMessages().add("Email deleted from server correctly [" + mailUser + "]");
        });
    }
}
