package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
            String mailAddress = jsonObjectReq.get("user").getAsString();
           OutputStream outputStream = socket.getOutputStream();
           PrintWriter writer = new PrintWriter(outputStream, true); // Auto-flushing enabled

           writer.println(generateResponse(mailAddress));
           writer.flush();
       }
       catch (IOException e){
           //qualcosa di più solido
           System.out.println(e);
       }
   }

    private JsonObject generateResponse(String mailAddress) {
        //apro file
        JsonObject response = new JsonObject();
        String filePathName = "/Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + mailAddress + ".txt";
        try {
            String fileUser = Files.readString(Paths.get(filePathName));
            JsonObject jsonObjectUser = JsonParser.parseString(fileUser).getAsJsonObject();

            long lastIdSent = jsonObjectUser.get("last_id_sent").getAsInt();
            JsonArray inboxArray = jsonObjectUser.getAsJsonArray("inbox");

            JsonArray newEmails = new JsonArray();
            long newLastId = lastIdSent;

            for (int i = inboxArray.size() - 1; i >= 0; i--) {
                JsonObject mail = inboxArray.get(i).getAsJsonObject();
                long mailId = mail.get("id").getAsInt();

                if (mailId > lastIdSent) {
                    newEmails.add(mail);
                    newLastId = Math.max(newLastId, mailId);
                }
            }

            jsonObjectUser.addProperty("last_id_sent", newLastId);
            Files.writeString(Paths.get(filePathName), jsonObjectUser.toString());
            response.add("inbox", newEmails);
        }
        catch (IOException e){
            System.out.println(e);
        }

        return response;
    }
}
