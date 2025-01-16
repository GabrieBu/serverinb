package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnableAuth implements Runnable{
    private final String clientReqString;
    private final Server server;

    public RunnableAuth(String clientReqString, Server server) {
        this.clientReqString = clientReqString;
        this.server = server;
    }

    private String unpackMail(String jsonAuth){
        JsonObject jsonObject = JsonParser.parseString(jsonAuth).getAsJsonObject();
        return jsonObject.get("typed_mail_user").getAsString();
    }

    private int unpackPort(String jsonAuth){
        JsonObject jsonObject = JsonParser.parseString(jsonAuth).getAsJsonObject();
        return Integer.parseInt(jsonObject.get("port").getAsString());
    }

    private void sendData(int clientPort, String userMail) {
        //forse va messo in un finally
        try {
            Socket clientSocket = new Socket("localhost", clientPort);
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            JsonObject response = new JsonObject();
            response.addProperty("type", "response_auth");
            if (checkEmailInFileNames(userMail)) {
                response.addProperty("authenticated", true);
                Platform.runLater(() -> {
                    server.getLogMessages().add("User [" + userMail + "] " + "authenticated");
                });
            } else {
                response.addProperty("authenticated", false);
            }
            writer.println(response);
            clientSocket.close();
        }
        catch( IOException e){
            System.out.println("Error reading inbox file: " + e.getMessage());
        }
    }

    private static boolean checkEmailInFileNames(String email) {
        File directory = new File("/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes");
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null) {
            System.out.println("No one Files found");
            return false;
        }

        for (File file : files) {
            String fileNameWithoutExtension = getFileNameWithoutExtension(file.getName());
            if (fileNameWithoutExtension.equals(email)) {
                System.out.println("File found " + fileNameWithoutExtension);
                return true;
            }
        }
        System.out.println("Files not found");
        return false;
    }

    private static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);  // remove the extension
    }

    public void run() {
        String typedMail = unpackMail(this.clientReqString);
        int clientPort = unpackPort(this.clientReqString);
        sendData(clientPort, typedMail);
    }
}
