package com.example.serverinb.Threads.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.locks.ReadWriteLock;

public interface FileManagerInt {
    public void updateFile(String emailAddress, JsonObject emailToBeSent, ReadWriteLock rwl);
    public long getNextPossibleId(JsonArray inbox);
    public String getFileNameWithoutExtension(String fileName);
    public boolean checkEmailInFileNames(String email);
}
