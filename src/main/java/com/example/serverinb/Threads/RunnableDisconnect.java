package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

public class RunnableDisconnect implements Runnable{
    private final String clientReqString;
    private final Server server;

    public RunnableDisconnect(String clientReqString, Server server) {
        this.clientReqString = clientReqString;
        this.server = server;
    }

    public void run() {
        JsonObject jsonObject = JsonParser.parseString(clientReqString).getAsJsonObject();
        String userMail = jsonObject.get("user").getAsString();
        Platform.runLater(() -> {
            server.getLogMessages().add("[" + userMail+ "] disconnected");
        });
    }
}
