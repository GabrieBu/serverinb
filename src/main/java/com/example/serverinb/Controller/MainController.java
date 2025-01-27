package com.example.serverinb.Controller;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.Dispatcher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.WindowEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {
    private Server server;
    @FXML
    private ListView<String> listViewLog;
    private static ExecutorService singleExecutor;

    @FXML
    public static void shutdown(WindowEvent event) {
        System.out.println("Server is now stopped");
        singleExecutor.shutdown();
        System.exit(0);
    }

    public void initListener(Server server){
        if(this.server == null)
            this.server = server;
        initListView();

        singleExecutor = Executors.newSingleThreadExecutor();
        Dispatcher dispatcher = new Dispatcher(server); //start the dispatcher
        singleExecutor.execute(dispatcher);
    }

    private void initListView() {
        listViewLog.setItems(server.getLogMessages()); //bind the view to the list in the model

        listViewLog.setCellFactory(param -> new ListCell<>() {
            protected void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(message);
                    label.setStyle("-fx-padding: 5; -fx-font-size: 14;"); // optional: Add styling
                    setGraphic(label); // set the Label as the graphic of the cell
                    setText(null);     // clear the cell's default text
                }
            }
        });
    }
}