package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;


public class RunnableForward implements Runnable {
    private final Server server;
    private final String clientReqString;

    public RunnableForward(String clientReqString, Server server) {
        this.server = server;
        this.clientReqString = clientReqString;
    }

    public void run() {
        System.out.println("Forward");
    }
}
