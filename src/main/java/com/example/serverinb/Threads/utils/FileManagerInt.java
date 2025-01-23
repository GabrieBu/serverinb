package com.example.serverinb.Threads.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface FileManagerInt {
    public void updateFile(String emailAddress, JsonObject emailToBeSent);
    public long getNextPossibleId(JsonArray inbox);
    public String getFileNameWithoutExtension(String fileName);
    public boolean checkEmailInFileNames(String email);
}
