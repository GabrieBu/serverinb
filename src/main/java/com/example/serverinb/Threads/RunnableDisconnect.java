package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnableDisconnect implements Runnable{
    private final String clientReqString;
    private final Server server;

    public RunnableDisconnect(String clientReqString, Server server) {
        this.clientReqString = clientReqString;
        this.server = server;
    }

    private String unpackMail(String jsonAuth){
        JsonObject jsonObject = JsonParser.parseString(jsonAuth).getAsJsonObject();
        return jsonObject.get("user").getAsString();
    }

    /*
    @TODO aggiornate last_id_sent a il minimo di un LONG
     */

    public void updateFile(String emailAddress){
        String filePathName = "/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + emailAddress + ".txt";
        try {
            String fileContent = Files.readString(Paths.get(filePathName));
            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            jsonObject.addProperty("last_id_sent", Long.MIN_VALUE);
            Files.writeString(Paths.get(filePathName), jsonObject.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error reading inbox file: " + e.getMessage());
        }
    }

    public void run() {
        String userMail = unpackMail(this.clientReqString);
        updateFile(userMail);
        Platform.runLater(() -> {
            server.getLogMessages().add("[" + userMail+ "] disconnected");
        });

    }
}
