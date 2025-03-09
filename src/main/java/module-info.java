module org.example.chatfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.chatfx to javafx.fxml;
    exports org.example.chatfx;
}