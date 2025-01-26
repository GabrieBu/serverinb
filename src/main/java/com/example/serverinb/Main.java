package com.example.serverinb;

import com.example.serverinb.Controller.MainController;
import com.example.serverinb.Model.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 768, 462);
        stage.setTitle("Server Log");
        stage.setScene(scene);
        stage.show();

        Server server;
        try {
            server = new Server();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
            return;
        }

        MainController contr = fxmlLoader.getController();
        contr.initListener(server);
    }

    public static void main(String[] args) {
        launch();
    }
}