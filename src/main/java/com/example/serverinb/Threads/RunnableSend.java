package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnableSend implements Runnable {
    private final Server server;
    private final String clientReqString;

    public RunnableSend(String clientReqString, Server server) {
        this.server = server;
        this.clientReqString=clientReqString;
    }

    private static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);  // remove the extension
    }

    private static boolean checkEmailInFileNames(String email) {
        File directory = new File("/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) {
            return false;
        }

        for (File file : files) {
            String fileNameWithoutExtension = getFileNameWithoutExtension(file.getName());
            if (fileNameWithoutExtension.equals(email)) {
                return true;
            }
        }
        return false;
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
            System.out.println("Written :" + jsonObject);
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
        return Long.MIN_VALUE + 1; //min index possible, Long.MIN_VALUE reserved for clients connecting
    }

    public void run() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
        JsonArray mailAddresses = mail.getAsJsonArray("to");
        String from = mail.get("from").getAsString();
        int clientPort = jsonObjectReq.get("port").getAsInt();

        for (int i = 0; i < mailAddresses.size(); i++) {
            String emailAddress = mailAddresses.get(i).getAsString();
            if (checkEmailInFileNames(emailAddress)) {
                updateFile(emailAddress, mail);
                Platform.runLater(() -> {
                    server.getLogMessages().add("Mail from [ " + from + "] sent to " + emailAddress);
                });
            }
            else{
                sendError(clientPort, mailAddresses);
                Platform.runLater(() -> {
                    server.getLogMessages().add("Mail from [ " + from + "] hasn't be sent. Address [ to edit + ] does not exist");
                });
            }
        }
    }

    private void sendError(int clientPort, JsonArray recipients) {
        JsonObject jsonObjectError = new JsonObject();
        jsonObjectError.addProperty("type", "send_error");
        StringBuilder listErrorRecipints = new StringBuilder();

        for(JsonElement errorRecipient : recipients){
            if(!checkEmailInFileNames(errorRecipient.getAsString())){
                listErrorRecipints.append(errorRecipient.getAsString());
            }
        }
        jsonObjectError.addProperty("to", listErrorRecipints.toString());
        try(Socket toClient = new Socket("localhost", clientPort)) {
            toClient.getOutputStream().write(jsonObjectError.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
