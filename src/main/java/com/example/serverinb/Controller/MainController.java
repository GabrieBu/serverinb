package com.example.serverinb.Controller;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.Listener;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {
    private Server server;
    @FXML
    private ListView<String> listViewLog;

    public void initListener(Server server){
        if(this.server == null)
            this.server = server;
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        Listener listener = new Listener(server);
        singleExecutor.execute(listener);

        System.out.println("Listener Partito");
    }
}