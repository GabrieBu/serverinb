module com.example.serverinb {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.serverinb to javafx.fxml;
    exports com.example.serverinb;
}