package client;

import client2.ChatController2; // Pour lier Client 2
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatController {

    @FXML private TextArea chatArea;
    @FXML private TextField messageInput;
    @FXML private TextField usernameInput;
    @FXML private Button connectButton;

    private PrintWriter out;
    private Socket socket;
    private String username;
    private boolean isConnected = false;

    private ChatController2 chatController2; // Référence vers Client 2 (optionnel en cas de liaison directe)

    // Méthode pour lier le contrôleur du client 2
    public void setChatController2(ChatController2 chatController2) {
        this.chatController2 = chatController2;
    }

    @FXML
    public void connectToServer() {
        if (isConnected) return;
        username = usernameInput.getText().trim();
        if (username.isEmpty()) {
            addSystemMessage("Veuillez entrer un nom d'utilisateur !");
            return;
        }
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Envoi du nom au serveur
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
                Platform.runLater(() -> addSystemMessage("Erreur de connexion: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void sendMessage() {
        String rawMessage = messageInput.getText().trim();
        if (rawMessage.isEmpty() || !isConnected) return;

        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String formattedMessage = "[" + timestamp + "] " + username + ": " + rawMessage;

        Platform.runLater(() -> addOwnMessage(formattedMessage));
        out.println(formattedMessage);
        messageInput.clear();

        // Optionnel : Envoi direct à Client 2 (si liaison directe)
        if (chatController2 != null) {
            chatController2.receiveMessage(formattedMessage);
        }
    }

    // Méthode pour recevoir un message (de Client 2 ou du serveur)
    public void receiveMessage(String message) {
        Platform.runLater(() -> addRemoteMessage(message));
    }

    // Ajoute un message envoyé (aligné à droite)
    private void addOwnMessage(String message) {
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label label = new Label(message);
        hbox.getChildren().add(label);
        chatArea.appendText(message + "\n"); // En complément, pour visualiser dans le TextArea
    }

    // Ajoute un message reçu (aligné à gauche)
    private void addRemoteMessage(String message) {
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label label = new Label(message);
        hbox.getChildren().add(label);
        chatArea.appendText(message + "\n");
    }

    // Ajoute un message système (centré)
    private void addSystemMessage(String message) {
        HBox hbox = new HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER);
        Label label = new Label(message);
        hbox.getChildren().add(label);
        chatArea.appendText(message + "\n");
    }
}
