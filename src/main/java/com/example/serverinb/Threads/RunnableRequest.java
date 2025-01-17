package com.example.serverinb.Threads;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnableRequest implements Runnable {
    private final String clientReqString;

    public RunnableRequest(String clientReqString) {
        this.clientReqString=clientReqString;
    }

    public int unpack() {
        JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
        return jsonObjectReq.get("port").getAsInt();
    }

   public void run(){
        try (Socket socket = new Socket("localhost", unpack())){
            JsonObject jsonObjectReq = JsonParser.parseString(clientReqString).getAsJsonObject();
            System.out.println(jsonObjectReq);
            String mailAddress = jsonObjectReq.get("user").getAsString();
            long lastIdSent = jsonObjectReq.get("last_id_received").getAsLong();
           OutputStream outputStream = socket.getOutputStream();
           PrintWriter writer = new PrintWriter(outputStream, true); // Auto-flushing enabled
           writer.println(generateResponse(mailAddress, lastIdSent));
           writer.flush();
       }
       catch (IOException e){
           //qualcosa di più solido
           System.out.println(e);
       }
   }

    private JsonObject generateResponse(String mailAddress, long lastIdSent) {
        JsonObject response = new JsonObject();
        String filePathName = "/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + mailAddress + ".txt";
        try {
            String fileUser = Files.readString(Paths.get(filePathName));
            JsonObject jsonObjectUser = JsonParser.parseString(fileUser).getAsJsonObject();

            JsonArray inboxArray = jsonObjectUser.getAsJsonArray("inbox");
            JsonArray newEmails = new JsonArray();

            for (int i = inboxArray.size() - 1; i >= 0; i--) {
                JsonObject mail = inboxArray.get(i).getAsJsonObject();
                long mailId = mail.get("id").getAsLong();

                if (mailId > lastIdSent) {
                    newEmails.add(mail);
                }
            }
            response.add("inbox", newEmails);
        }
        catch (IOException e){
            System.out.println(e);
        }

        return response;
    }
}
