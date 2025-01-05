package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

public class RunnableHandshakeDisconnect implements Runnable{
    private final String clientReqString;
    private final Server server;
    private final String typeReqString;

    public RunnableHandshakeDisconnect(String clientReqString, Server server, String typeReqString) {
        this.clientReqString = clientReqString;
        this.server = server;
        this.typeReqString = typeReqString;
    }

    private String unpackMail(String jsonAuth){
        JsonObject jsonObject = JsonParser.parseString(jsonAuth).getAsJsonObject();
        return jsonObject.get("typed_mail_user").getAsString();
    }

    private int unpackPort(String jsonAuth){
        JsonObject jsonObject = JsonParser.parseString(jsonAuth).getAsJsonObject();
        return Integer.parseInt(jsonObject.get("port").getAsString());
    }

    public void run() {
        String typedMail = unpackMail(this.clientReqString);
        int clientPort = unpackPort(this.clientReqString);

        if(this.typeReqString.equals("handshake")) {
            Platform.runLater(() -> {
                server.getLogMessages().add("[" + typedMail + "] reserved port " + clientPort);
            });
            this.server.putPort(typedMail, clientPort);
        }
        else if(this.typeReqString.equals("disconnect")) {
            Platform.runLater(() -> {
                server.getLogMessages().add("[" + typedMail + "] disconnected, releasing port " + clientPort);
            });
            this.server.deletePort(typedMail);
        }
    }
}
