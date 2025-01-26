package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.utils.FileManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class RunnableAuth implements Runnable{
    private final String clientReqString;
    private final Server server;
    private final FileManager fileManager;

    public RunnableAuth(String clientReqString, Server server) {
        this.clientReqString = clientReqString;
        this.server = server;
        fileManager = new FileManager();
    }

    public void run() {
        JsonObject jsonObject = JsonParser.parseString(clientReqString).getAsJsonObject();
        String userMail = jsonObject.get("typed_mail_user").getAsString();
        int clientPort = jsonObject.get("port").getAsInt();

        try ( Socket clientSocket = new Socket("localhost", clientPort);
              PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);){
            JsonObject response = new JsonObject();
            response.addProperty("type", "response_auth");
            if (fileManager.checkEmailInFileNames(userMail)) {
                response.addProperty("authenticated", true);
                Platform.runLater(() -> {
                    server.getLogMessages().add("User [" + userMail + "] " + "authenticated");
                });
            } else {
                response.addProperty("authenticated", false);
            }
            writer.println(response);
        }
        catch( IOException e){
            throw new RuntimeException("Error reading inbox file: " + e.getMessage());
        }
    }
}
