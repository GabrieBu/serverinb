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

public class RunnableReply implements Runnable {
    private final String clientReqString;
    private final Server server;
    private final String typeReqString;
    private final FileManager fileManager;
    private final FileAccessController fileAccessController;

    public RunnableReply(String clientReqString, Server server, String typeReqString,FileAccessController fileAccessController) {
        this.clientReqString = clientReqString;
        this.server = server;
        this.typeReqString = typeReqString; //useful for either reply or reply_all
        fileManager = new FileManager();
        this.fileAccessController = fileAccessController;
    }

    public void run() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
        String from = mail.get("from").getAsString();
        JsonArray allMails = mail.getAsJsonArray("to");
        int clientPort = jsonObjectReq.get("port").getAsInt();

        if(typeReqString.equals("reply")) {
            String toRecipient = allMails.get(0).getAsString(); //old "from" user
            fileManager.updateFile(toRecipient, mail,fileAccessController);
            Platform.runLater(() -> {
                server.getLogMessages().add("[ " + from + "] replied to [" + toRecipient + "]");
            });
        }
        else{ //reply_all
            for (int i = 0; i < allMails.size(); i++) {
                String toRecipient = allMails.get(i).getAsString();
                fileManager.updateFile(toRecipient, mail,fileAccessController);
                Platform.runLater(() -> {
                        server.getLogMessages().add("[ " + from + "] replied to [" + toRecipient + "]");
                });
            }
        }
        //feedback to the client (it can be only positive)
        JsonObject response = new JsonObject();
        response.addProperty("type", "send_ok");
        try(Socket toClient = new Socket("localhost", clientPort)) {
            toClient.getOutputStream().write(response.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error sending client feedback: " + e.getMessage());
        }
    }
}
