package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnableReply implements Runnable {
    private final String clientReqString;
    private final Server server;
    private final String typeReqString;

    public RunnableReply(String clientReqString, Server server, String typeReqString) {
        this.clientReqString = clientReqString;
        this.server = server;
        this.typeReqString = typeReqString;
    }

    public void run() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
        String from = mail.get("from").getAsString();
        JsonArray allMails = mail.getAsJsonArray("to");

        if(typeReqString.equals("reply")) {
            String toRecipient = allMails.get(0).getAsString();
            updateFile(toRecipient, mail);
            Platform.runLater(() -> {
                server.getLogMessages().add("[ " + from + "] replied to [" + toRecipient + "]");
            });
        }
        else{ //reply_all
            for (int i = 0; i < allMails.size(); i++) {
                String toRecipient = allMails.get(i).getAsString();
                updateFile(toRecipient, mail);
                Platform.runLater(() -> {
                        server.getLogMessages().add("[ " + from + "] replied to [" + toRecipient + "]");
                });
            }

        }
    }

    public void updateFile(String emailAddress, JsonObject emailToBeSent){
        String filePathName = "/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + emailAddress + ".txt";
        try {
            String fileContent = Files.readString(Paths.get(filePathName));
            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonArray inbox = jsonObject.getAsJsonArray("inbox");
            emailToBeSent.addProperty("id", getNextPossibleId(inbox));
            inbox.add(emailToBeSent);
            Files.writeString(Paths.get(filePathName), jsonObject.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error reading inbox file: " + e.getMessage());
        }
    }

    private long getNextPossibleId(JsonArray inbox) {
        if(!inbox.isEmpty()) {
            JsonElement lastElementInbox = inbox.get(inbox.size() - 1);
            JsonObject lastObject = lastElementInbox.getAsJsonObject();
            return lastObject.get("id").getAsLong() + 1;
        }
        return Long.MIN_VALUE + 1;
    }
}
