package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;


public class RunnableForward implements Runnable {
    private final Server server;
    private final String clientReqString;

    public RunnableForward(String clientReqString, Server server) {
        this.server = server;
        this.clientReqString = clientReqString;
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

    private long getNextPossibleId(JsonArray inbox) {
        if(!inbox.isEmpty()) {
            JsonElement lastElementInbox = inbox.get(inbox.size() - 1);
            JsonObject lastObject = lastElementInbox.getAsJsonObject();
            return lastObject.get("id").getAsLong() + 1;
        }
        return Long.MIN_VALUE + 1;
    }

    public void updateFile(String emailAddress, JsonObject emailToBeSent){
        String filePathName = "/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + emailAddress + ".txt";
        try {
            String fileContent = Files.readString(Paths.get(filePathName));
            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonArray inbox = jsonObject.getAsJsonArray("inbox");
            emailToBeSent.addProperty("id", getNextPossibleId(inbox));
            inbox.add(emailToBeSent); //dovrei inserire in prima posizione in tempo ragionevole
            Files.writeString(Paths.get(filePathName), jsonObject.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error reading inbox file: " + e.getMessage());
        }
    }

    private void sendFile(JsonObject emailToBeSent, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // Auto-flushing enabled
            writer.println(emailToBeSent.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError(Socket socket, String to){
        JsonObject jsonObjectError = new JsonObject();
        jsonObjectError.addProperty("type", "send_error");
        jsonObjectError.addProperty("to", to);
        try{
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true); // Auto-flushing enabled
            writer.println(jsonObjectError);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
        JsonArray allMails = mail.getAsJsonArray("to");
        String from = mail.get("from").getAsString();
        for (int i = 0; i < allMails.size(); i++) {
            String emailAddress = allMails.get(i).getAsString();
            if(checkEmailInFileNames(emailAddress)) {
                updateFile(emailAddress, mail);
                Platform.runLater(() -> {
                    server.getLogMessages().add("Mail from [ " + from + "] forwarded to " + emailAddress);
                });
            }
            else{
                Platform.runLater(() -> {
                    server.getLogMessages().add("Mail from [ " + from + "] hasn't be forwarded. Address [" + emailAddress + "] does not exist");
                });
            }
        }
    }
}
