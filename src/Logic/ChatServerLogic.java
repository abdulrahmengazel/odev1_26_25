package Logic;

import Ui.ChatServerUI;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

public class ChatServerLogic {

    private final ChatServerUI ui;
    private ServerSocket serverSocket;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public ChatServerLogic(ChatServerUI ui) {
        this.ui = ui;
    }

    public boolean isRunning() {
        return running;
    }

    public void startServer(String portText) {
        try {
            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) throw new NumberFormatException();
            serverSocket = new ServerSocket(port);
            running = true;
            ui.updateServerStatus(true, port);
            ui.addLog("Server started on port " + port);

            new Thread(() -> {
                while (running) {
                    try {
                        Socket sock = serverSocket.accept();
                        ui.addLog("Incoming connection from " + sock.getInetAddress().getHostAddress());
                        new Thread(new ClientHandler(sock)).start();
                    } catch (IOException e) {
                        if (running) ui.addLog("ERROR: Accept failed – " + e.getMessage());
                    }
                }
            }, "accept-thread").start();
        } catch (NumberFormatException e) {
            ui.showErrorMessage("Invalid port number.", "Error");
        } catch (IOException e) {
            ui.showErrorMessage("Could not start server:\n" + e.getMessage(), "Error");
        }
    }

    public void stopServer() {
        running = false;
        broadcastToAll("SERVER|Server is shutting down.");
        for (ClientHandler ch : clients.values()) ch.close();
        clients.clear();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        ui.addLog("Server stopped.");
        ui.updateServerStatus(false, 0);
        ui.updateUserList(clients.keySet());
    }

    public void logClientIp(String username) {
        ClientHandler ch = clients.get(username);
        if (ch != null)
            ui.addLog("INFO: " + username + " connected from " + ch.socket.getInetAddress().getHostAddress());
    }

    public void sendAnnouncement(String text) {
        if (!running) {
            ui.showErrorMessage("Start the server first.", "Server Offline");
            return;
        }

        String body = text == null ? "" : text.trim();
        if (body.isEmpty()) {
            ui.showErrorMessage("Announcement cannot be empty.", "Invalid Announcement");
            return;
        }

        broadcastToAll("SERVER|" + body);
        ui.addLog("ANNOUNCEMENT: " + body);
    }

    void broadcastToAll(String message) {
        broadcast(message, null);
    }

    void broadcast(String message, String excludeUser) {
        for (Map.Entry<String, ClientHandler> e : clients.entrySet()) {
            if (excludeUser == null || !e.getKey().equals(excludeUser)) e.getValue().send(message);
        }
    }

    void sendToUser(String username, String message) {
        ClientHandler ch = clients.get(username);
        if (ch != null) ch.send(message);
    }

    void broadcastUserList() {
        String list = String.join(",", clients.keySet());
        broadcastToAll("USERLIST|" + list);
        ui.updateUserList(clients.keySet());
    }

    // ── ClientHandler ─────────────────────────────────────────────────────────
    class ClientHandler implements Runnable {
        final  Socket       socket;
        private PrintWriter  out;
        private String       username;

        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                // ── Handshake: expect JOIN|<username> ──
                String first = in.readLine();
                if (first == null || !first.startsWith("JOIN|")) {
                    socket.close(); return;
                }
                username = first.substring(5).trim();

                if (username.isEmpty()) {
                    send("ERROR|Username cannot be empty."); socket.close(); return;
                }
                if (clients.containsKey(username)) {
                    send("ERROR|Username '" + username + "' is already taken."); socket.close(); return;
                }

                clients.put(username, this);
                ui.addLog("JOIN: " + username + " (" + socket.getInetAddress().getHostAddress() + ")");
                broadcastToAll("LOG|" + username + " joined the chat.");
                broadcastUserList();

                // ── Main loop ──
                String line;
                while ((line = in.readLine()) != null) {
                    process(line);
                }

            } catch (IOException e) {
                if (username != null) ui.addLog("Connection lost: " + username + " – " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void process(String line) {
            String ts = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());

            if (line.startsWith("MSG|")) {
                String body = line.substring(4);
                // Private message?
                if (body.startsWith("@")) {
                    int sp = body.indexOf(' ');
                    if (sp > 1) {
                        String target = body.substring(1, sp);
                        String pm     = body.substring(sp + 1);
                        if (clients.containsKey(target)) {
                            sendToUser(target, "PRIVATE|" + username + "|" + ts + "|" + pm);
                            send("PRIVATE_SENT|" + target + "|" + ts + "|" + pm);
                            ui.addLog("PM: " + username + " → " + target + ": " + pm);
                        } else {
                            send("ERROR|User @" + target + " is not online.");
                        }
                    } else {
                        send("ERROR|Usage: @username <message>");
                    }
                } else {
                    // Broadcast
                    broadcastToAll("MSG|" + username + "|" + ts + "|" + body);
                    ui.addLog("MSG [" + username + "]: " + body);
                }

            } else if (line.startsWith("FILE|")) {
                // FILE|<filename>|<base64>
                String[] p = line.split("\\|", 3);
                if (p.length == 3) {
                    String filename = p[1];
                    String data     = p[2];
                    ui.addLog("FILE: " + username + " shared '" + filename + "'");
                    broadcastToAll("FILE|" + username + "|" + ts + "|" + filename + "|" + data);
                }

            } else if (line.equals("EXIT")) {
                disconnect();
            }
        }

        void send(String message) {
            if (out != null && !socket.isClosed()) out.println(message);
        }

        void close() {
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        }

        void disconnect() {
            if (username != null && clients.remove(username) != null) {
                ui.addLog("LEAVE: " + username);
                broadcastToAll("LOG|" + username + " left the chat.");
                broadcastUserList();
                username = null;
            }
            close();
        }
    }
}