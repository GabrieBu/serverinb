package com.example.serverinb.Threads.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class FileManager implements FileManagerInt {
    public  String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);  // remove the extension
    }

    public boolean checkEmailInFileNames(String email) {
        System.out.println(email);
        File directory = new File("C:\\Users\\andre\\Desktop\\Prog3\\PROGETTO_SERVER\\NEWserver\\serverinb\\src\\main\\java\\com\\example\\serverinb\\Storage\\inboxes");
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
   ///Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/
    public void updateFile(String emailAddress, JsonObject emailToBeSent, ReadWriteLock rwl){
        /*Lock wl = rwl.writeLock();*/
        String filePathName = "C:\\Users\\andre\\Desktop\\Prog3\\PROGETTO_SERVER\\NEWserver\\serverinb\\src\\main\\java\\com\\example\\serverinb\\Storage\\inboxes" + emailAddress + ".txt";
        try {
            /* wl.lock();*/
            String fileContent = Files.readString(Paths.get(filePathName));
            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();

            JsonArray inbox = jsonObject.getAsJsonArray("inbox");
            emailToBeSent.addProperty("id", getNextPossibleId(inbox));
            inbox.add(emailToBeSent);
            Files.writeString(Paths.get(filePathName), jsonObject.toString());
            System.out.println("Written :" + jsonObject);
        } catch (IOException e) {
            throw new RuntimeException("Error reading inbox file: " + e.getMessage());
        }finally{
          /*  wl.unlock();*/
        }
    }

    public long getNextPossibleId(JsonArray inbox) {
        if(!inbox.isEmpty()) {
            JsonElement lastElementInbox = inbox.get(inbox.size() - 1);
            JsonObject lastObject = lastElementInbox.getAsJsonObject();
            return lastObject.get("id").getAsLong() + 1;
        }
        return Long.MIN_VALUE + 1; //min index possible, Long.MIN_VALUE reserved for clients connecting
    }

    //used only in delete task
    public void rewriteFile(String mailUser, int indexToRemove,ReadWriteLock rwl) throws IOException {
       /* Lock wl=rwl.writeLock();*/
        try {
            String filePath = "C:\\Users\\andre\\Desktop\\Prog3\\PROGETTO_SERVER\\NEWserver\\serverinb\\src\\main\\java\\com\\example\\serverinb\\Storage\\inboxes" + mailUser + ".txt";
            String fileContent = Files.readString(Paths.get(filePath));
            JsonObject jsonObjectFile = JsonParser.parseString(fileContent).getAsJsonObject();
            /*  wl.lock();*/
            JsonArray inbox = jsonObjectFile.getAsJsonArray("inbox");
            inbox.remove(indexToRemove);
            JsonObject newContentFile = new JsonObject();
            newContentFile.add("inbox", inbox);

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(newContentFile.toString());
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }catch (Exception e){
            throw new RuntimeException("Error removing inbox file: " + e.getMessage());
        }finally {
            /* wl.unlock();*/
        }


    }
}
