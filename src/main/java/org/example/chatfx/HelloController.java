package org.example.chatfx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;

    private PrintWriter out; // For sending messages
    private BufferedReader in; // For receiving messages

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Connect to the server (replace "localhost" and 5000 with your server details)
            Socket socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a thread to listen for incoming messages
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Listen for incoming messages from the server
    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String finalMessage = message; // Effectively final for lambda
                javafx.application.Platform.runLater(() -> appendMessage("server", finalMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send message button action
    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message); // Send message to the server
            appendMessage("client", message); // Display the message in the chat area
            messageField.clear(); // Clear the input field
        }
    }

    // Append message to the chat area
    public void appendMessage(String sender, String message) {
        String conversation = chatArea.getText();
        chatArea.setText(conversation + "\n" + sender + ": " + message);
    }
}

