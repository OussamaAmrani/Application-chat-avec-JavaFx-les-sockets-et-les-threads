package client2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;

public class ChatController2 {
    @FXML private TextArea chatArea;
    @FXML private TextField messageInput;
    @FXML private TextField usernameInput;
    @FXML private Button connectButton;

    private PrintWriter out;
    private Socket socket;
    private BufferedReader in;
    private String username;
    private boolean isConnected = false;

    @FXML
    public void connectToServer() {
        if (isConnected) return;
        username = usernameInput.getText().trim();
        if (username.isEmpty()) {
            usernameInput.setStyle("-fx-border-color: red;");
            return;
        }
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Envoie du nom d'utilisateur au serveur
                out.println(username);
                isConnected = true;

                Platform.runLater(() -> {
                    usernameInput.setEditable(false);
                    connectButton.setText("Connecté");
                    connectButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    addSystemMessage("Connecté en tant que " + username);
                });

                String message;
                while ((message = in.readLine()) != null) {
                    final String receivedMessage = message;
                    Platform.runLater(() -> addRemoteMessage(receivedMessage));
                }
            } catch (IOException e) {
                Platform.runLater(() -> addSystemMessage("Connexion échouée"));
            }
        }).start();
    }

    @FXML
    public void sendMessage() {
        String rawMessage = messageInput.getText().trim();
        if (rawMessage.isEmpty() || !isConnected) return;
        // Envoie uniquement le message brut au serveur
        out.println(rawMessage);
        messageInput.clear(); // Vide le champ après l'envoi
    }

    public void receiveMessage(String message) {
        Platform.runLater(() -> addRemoteMessage(message));
    }

    protected void addRemoteMessage(String message) {
        chatArea.appendText(message + "\n");
    }

    protected void addSystemMessage(String message) {
        chatArea.appendText(message + "\n");
    }
}
