package com.example.serverinb.Threads;

import com.example.serverinb.Threads.utils.FileAccessController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;

public class RunnableRequest implements Runnable {
    private final String clientReqString;
    private final FileAccessController fileAccessController;

    public RunnableRequest(String clientReqString, FileAccessController fileAccessController) {
        this.clientReqString = clientReqString;
        this.fileAccessController = fileAccessController;
    }

   public void run(){
       JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();

        try (Socket socket = new Socket("localhost", jsonObjectReq.get("port").getAsInt())){
            String mailAddress = jsonObjectReq.get("user").getAsString();
            long lastIdSent = jsonObjectReq.get("last_id_received").getAsLong();
           OutputStream outputStream = socket.getOutputStream();
           PrintWriter writer = new PrintWriter(outputStream, true); // Auto-flushing enabled
           writer.println(generateResponse(mailAddress, lastIdSent)); //send inbox's changes to the client (first time all mails)
           writer.flush();
       }
       catch (IOException e){
           throw new RuntimeException("Error getting localhost port: " + e.getMessage());
       }
   }

    private JsonObject generateResponse(String mailAddress, long lastIdSent) {
        JsonObject response = new JsonObject();
        String filePathName = "//Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + mailAddress + ".txt";
        Lock readLock = fileAccessController.getReadLock(filePathName);
        try {
            readLock.lock();
            String fileUser = Files.readString(Paths.get(filePathName)); //reads the file
            JsonObject jsonObjectUser = JsonParser.parseString(fileUser).getAsJsonObject(); //cast ot JsonObject
            JsonArray inboxArray = jsonObjectUser.getAsJsonArray("inbox"); //convert inbox array into JsonArray

            JsonArray newEmails = new JsonArray(); //inbox changes (to append to response Json)
            for (int i = 0 ; i < inboxArray.size(); i++) {
                JsonObject mail = inboxArray.get(i).getAsJsonObject();
                long mailId = mail.get("id").getAsLong();

                if (mailId > lastIdSent) {
                    newEmails.add(mail);
                }
            }
            response.add("inbox", newEmails);
        }
        catch (IOException e){
            throw new RuntimeException("Error generating response for the client: " + e.getMessage());
        }finally {
            readLock.unlock();
        }

        return response;
    }
}
