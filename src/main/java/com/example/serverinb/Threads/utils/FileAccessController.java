package com.example.serverinb.Threads.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileAccessController {
    private final ConcurrentHashMap<String, ReadWriteLock> fileLocks;

    public FileAccessController(){
        fileLocks = new ConcurrentHashMap<>();
    }

    // Restituisce il ReadWriteLock associato al file
    public ReadWriteLock getLock(String fileName) {
        return fileLocks.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock()); //computeIfAbsent is Thread-safe
    }

    // Restituisce il lock di lettura per un file
    public Lock getReadLock(String fileName) {
        return getLock(fileName).readLock();
    }

    // Restituisce il lock di scrittura per un file
    public Lock getWriteLock(String fileName) {
        return getLock(fileName).writeLock();
    }
}

