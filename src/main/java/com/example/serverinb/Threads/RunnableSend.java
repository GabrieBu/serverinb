package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.utils.FileAccessController;
import com.example.serverinb.Threads.utils.FileManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;

public class RunnableSend implements Runnable {
    private final Server server;
    private final String clientReqString;
    private final FileManager fileManager;
    private final FileAccessController fileAccessController;

    public RunnableSend(String clientReqString, Server server,FileAccessController fileAccessController) {
        this.server = server;
        this.clientReqString=clientReqString;
        this.fileManager = new FileManager();
        this.fileAccessController = fileAccessController;
    }

    public void run() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
        JsonArray recipients = mail.getAsJsonArray("to");
        String from = mail.get("from").getAsString();
        int clientPort = jsonObjectReq.get("port").getAsInt();

        JsonArray invalidRecipients = new JsonArray();
        for (int i = 0; i < recipients.size(); i++) {
            String emailAddress = recipients.get(i).getAsString();
            if (fileManager.checkEmailInFileNames(emailAddress)) {
                fileManager.updateFile(emailAddress, mail, fileAccessController); //it will be sent to correct addresses
                Platform.runLater(() -> {
                    server.getLogMessages().add("Mail from [ " + from + "] sent to " + emailAddress);
                });
            }
            else{
                invalidRecipients.add(emailAddress);
                Platform.runLater(() -> {
                    server.getLogMessages().add("Mail from [ " + from + "] hasn't be sent to [ " + emailAddress + " ]. Some addresses do not exist");
                });
            }
        }
        sendConfirmation(clientPort, invalidRecipients); //either positive or negative
    }

    private void sendConfirmation(int clientPort, JsonArray recipients) {
        JsonObject response = new JsonObject();
        if(recipients.isEmpty())
            response.addProperty("type", "send_ok");
        else {
            response.addProperty("type", "send_error");
            response.add("invalid_recipients", recipients);
        }
        try(Socket toClient = new Socket("localhost", clientPort)) {
            toClient.getOutputStream().write(response.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error sending back feedback to the client: " + e.getMessage());
        }
    }
}
