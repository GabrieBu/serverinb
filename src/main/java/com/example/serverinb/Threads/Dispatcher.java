package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.utils.FileAccessController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Dispatcher implements Runnable{
    private final Server server;
    private final FileAccessController fileAccessController; // Gestisce i ReadWriteLock


    public Dispatcher(Server server) {
        this.server = server;
        this.fileAccessController = new FileAccessController();
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(server.getPoolSize());
        try {
            ServerSocket serverSocket = new ServerSocket(this.server.getServerPort());
            while(true) {
                System.out.println("Listening on port " + server.getServerPort());
                Socket sock = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String clientReqString = reader.readLine();
                String typeRequestString = unpack(clientReqString);
                sock.close();

                switch (typeRequestString) {
                    case "authentication":
                        executor.execute(new RunnableAuth(clientReqString, server));
                        break;
                    case "forward":
                    case "send":
                        System.out.println("Send Arrived: " + clientReqString);
                        executor.execute(new RunnableSend(clientReqString, server,fileAccessController));
                        break;
                    case "delete":
                        executor.execute(new RunnableDelete(clientReqString, server,fileAccessController));
                        break;
                    case "reply":
                    case "reply_all":
                        executor.execute(new RunnableReply(clientReqString, server, typeRequestString,fileAccessController));
                        break;
                    case "disconnect":
                        executor.execute(new RunnableDisconnect(clientReqString, server));
                        break;
                    case "request":
                        executor.execute(new RunnableRequest(clientReqString,fileAccessController));
                        break;
                    default:
                        System.out.println("Unknown command: " + typeRequestString);
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error: Switch dispatcher " + e.getMessage()); //handle better
        }
        finally {
            executor.shutdown();
        }
    }

    private String unpack(String jsonReq){
        JsonObject jsonObject = JsonParser.parseString(jsonReq).getAsJsonObject();
        return jsonObject.get("type").getAsString();
    }
}


