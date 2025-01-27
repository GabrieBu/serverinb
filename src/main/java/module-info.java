module com.example.serverinb {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.serverinb to javafx.fxml;
    exports com.example.serverinb;
    exports com.example.serverinb.Controller;
    exports com.example.serverinb.Model;
    opens com.example.serverinb.Controller to javafx.fxml;
}