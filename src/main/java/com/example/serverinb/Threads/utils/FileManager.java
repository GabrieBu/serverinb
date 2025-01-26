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

public class FileManager implements FileManagerInt {
    public  String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);  // remove the extension
    }

    public boolean checkEmailInFileNames(String email) {
        File directory = new File("//Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/");
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

    public void updateFile(String emailAddress, JsonObject emailToBeSent, FileAccessController fileAccessController){
        String filePathName = "//Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + emailAddress + ".txt";
        Lock writeLock = fileAccessController.getWriteLock(filePathName);
        try {
            writeLock.lock();
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
          writeLock.unlock();
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
    public void rewriteFile(String mailUser, int indexToRemove,FileAccessController fileAccessController) throws IOException {
        String filePath = "//Users/gabrielebuoso/IdeaProjects/serverinb/serverinb/src/main/java/com/example/serverinb/Storage/inboxes/" + mailUser + ".txt";
        Lock writeLock = fileAccessController.getWriteLock(filePath);
        try {
            writeLock.lock();
            String fileContent = Files.readString(Paths.get(filePath));
            JsonObject jsonObjectFile = JsonParser.parseString(fileContent).getAsJsonObject();

            JsonArray inbox = jsonObjectFile.getAsJsonArray("inbox");
            if(!inbox.isEmpty()) {
                inbox.remove(indexToRemove);
                JsonObject newContentFile = new JsonObject();
                newContentFile.add("inbox", inbox);

                try (FileWriter writer = new FileWriter(filePath)) {
                    writer.write(newContentFile.toString());
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }
        }catch (Exception e){
            throw new RuntimeException("Error removing inbox file: " + e.getMessage());
        }finally {
            writeLock.unlock();
        }
    }
}
