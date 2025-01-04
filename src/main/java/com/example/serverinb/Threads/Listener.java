package com.example.serverinb.Threads;

import com.example.serverinb.Model.Server;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener implements Runnable {
    private ExecutorService executor;
    private Server server;

    public Listener(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        executor = Executors.newFixedThreadPool(server.getPoolSize());

        try {
            ServerSocket serverSocket = new ServerSocket(server.getServerPort());
            while(true) {
                //logger.logMessage("Server listening on port " + SERVER_PORT);
                Socket sock = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String clientReqString = reader.readLine();
                String typeRequestString = unpack(clientReqString);
                sock.close();

                switch (typeRequestString) {
                    case "authentication":
                        executor.execute(new RunnableAuth(clientReqString, server));
                        break;
                    case "send":
                        executor.execute(new RunnableSend(clientReqString, server));
                        break;
                    case "delete":
                        executor.execute(new RunnableDelete(clientReqString));
                        break;
                    case "reply":
                    case "reply_all":
                        executor.execute(new RunnableReply(clientReqString, server, typeRequestString));
                        break;
                    case "forward":
                        executor.execute(new RunnableForward(clientReqString, server));
                    case "handshake":
                        executor.execute(new RunnableHandshakeDisconnect(clientReqString, server, typeRequestString));
                        break;
                    case "disconnect":
                        executor.execute(new RunnableHandshakeDisconnect(clientReqString, server, typeRequestString));
                    case "ping":
                        //ignore ping
                        break;
                    default:
                        System.out.println("Unknown command: " + typeRequestString);
                }
            }
        }
        catch (Exception e) {
            //logger.logError("Server is not listening. Error occured: " + e.getMessage());
            //non so che fare qui
        }
        finally {
            //logger.logMessage("Server is going to be stopped!");
            executor.shutdown();
        }
    }

    private String unpack(String jsonReq){
        JsonObject jsonObject = JsonParser.parseString(jsonReq).getAsJsonObject();
        return jsonObject.get("type").getAsString();
    }
}


