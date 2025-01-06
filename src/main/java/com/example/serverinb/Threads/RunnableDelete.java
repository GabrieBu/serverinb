package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnableDelete implements Runnable{
    private final String clientReqString;
    private final Server server;

    public RunnableDelete(String cientReqString, Server server) {
        this.clientReqString = cientReqString;
        this.server = server;
    }

    public void run() {
        try {
            JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
            String mailUser = jsonObjectReq.get("user").getAsString();
            String filePathName = "/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + mailUser + ".txt";

            String fileContent = Files.readString(Paths.get(filePathName));
            JsonObject jsonObjectFile = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonArray inbox = jsonObjectFile.getAsJsonArray("inbox");
            int indexToRemove = jsonObjectReq.get("index_to_remove").getAsInt();
            inbox.remove(indexToRemove);
            rewriteFile(inbox, filePathName);
            Platform.runLater(() -> {
                server.getLogMessages().add("Email deleted from server correctly [" + mailUser + "]");
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rewriteFile(JsonArray inbox, String filePath) {
        JsonObject newContentFile = new JsonObject();
        newContentFile.add("inbox", inbox);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(newContentFile.toString());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
