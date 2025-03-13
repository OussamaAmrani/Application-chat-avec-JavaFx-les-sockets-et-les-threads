module ChatApplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    exports server;
    exports client;
    exports client2;

    opens client to javafx.fxml;
    opens client2 to javafx.fxml;
}