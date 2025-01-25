package com.example.serverinb.Controller;

import com.example.serverinb.Model.Server;
import com.example.serverinb.Threads.Dispatcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
        initListView();
        try{
            ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
            Dispatcher dispatcher = new Dispatcher(server); //start the dispatcher
            singleExecutor.execute(dispatcher);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
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
                    label.setStyle("-fx-padding: 5; -fx-font-size: 14;"); // Optional: Add styling
                    setGraphic(label); // Set the Label as the graphic of the cell
                    setText(null);     // Clear the cell's default text
                }
            }
        });
    }
}