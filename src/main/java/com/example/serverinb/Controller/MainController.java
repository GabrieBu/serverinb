package com.example.serverinb.Controller;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.Listener;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class MainController {
    private Server server;
    @FXML
    private ListView<String> listViewLog;

    public void initModel(Server server){
        if(this.server == null)
            this.server = server;
        startListener();
    }

    public void startListener() {
        if (this.server == null) {
            throw new IllegalStateException("Inbox must be initialized before starting the listener.");
        }
        Listener listener = new Listener(server);
        Thread thread = new Thread(listener);
        thread.start();
    }
}