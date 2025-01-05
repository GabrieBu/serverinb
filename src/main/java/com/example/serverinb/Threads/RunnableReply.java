package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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
        if(typeReqString.equals("reply")) {
            JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
            JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
            String from = mail.get("from").getAsString();
            JsonArray allMails = mail.getAsJsonArray("to");
            String toRecipient = allMails.get(0).getAsString();

            try {
                if(this.server.hasKey(toRecipient)) {
                    int clientPort = server.getPort(toRecipient);
                    Socket clientSocket = new Socket("localhost", clientPort);
                    sendFile(jsonObjectReq, clientSocket);
                    clientSocket.close();
                    //logger.logSuccess(from + "replied to " + toRecipient + " correctly on port " + clientPort);
                    System.out.println(from + "replied to " + toRecipient + " correctly on port " + clientPort);
                }
                updateFile(toRecipient, mail);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{ //reply_all
            JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
            JsonObject mail = jsonObjectReq.get("mail").getAsJsonObject();
            String from = mail.get("from").getAsString();
            JsonArray allMails = mail.getAsJsonArray("to");
            try {
                for (int i = 0; i < allMails.size(); i++) {
                    String toRecipient = allMails.get(i).getAsString();
                    if(this.server.hasKey(toRecipient)) {
                        int clientPort = server.getPort(toRecipient);
                        Socket clientSocket = new Socket("localhost", clientPort);
                        sendFile(jsonObjectReq, clientSocket);
                        clientSocket.close();
                        //logger.logSuccess(from + " replies to " + toRecipient + " correctly on port " + clientPort);
                        System.out.println(from + "replied to " + toRecipient + " on port " + clientPort);
                    }
                    updateFile(toRecipient, mail);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
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

    public void updateFile(String emailAddress, JsonObject emailToBeSent){
        String filePathName = "serverinb/src/main/java/com/example/serverinb/Storage/inboxes" + emailAddress + ".txt";
        try {
            String fileContent = Files.readString(Paths.get(filePathName));
            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonArray inbox = jsonObject.getAsJsonArray("inbox");
            inbox.add(emailToBeSent); //dovrei inserire in prima posizione in tempo ragionevole
            Files.writeString(Paths.get(filePathName), jsonObject.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error reading inbox file: " + e.getMessage());
        }
    }
}
