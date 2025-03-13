package server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    // Ensemble synchronisé pour conserver les flux de sortie de tous les clients connectés.
    private static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Le serveur de chat démarre... ");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Accepte une nouvelle connexion et lance un thread pour gérer le client
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Initialisation des flux d'entrée et de sortie
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientWriters.add(out);

                // Le premier message envoyé par le client doit être son nom d'utilisateur
                username = in.readLine();
                String joinMessage = username + " a rejoint le chat.";
                broadcast(joinMessage);
                System.out.println(joinMessage);

                String message;
                while ((message = in.readLine()) != null) {
                    // Ajoute un timestamp au message
                    String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    String fullMessage = "[" + timestamp + "] " + username + " : " + message;
                    broadcast(fullMessage);
                    System.out.println(fullMessage);
                }
            } catch (IOException e) {
                System.err.println("Erreur de connexion avec " + username + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientWriters.remove(out);
                String leaveMessage = username + " a quitté le chat.";
                broadcast(leaveMessage);
                System.out.println(leaveMessage);
            }
        }

        // Diffuse le message à tous les clients connectés
        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
