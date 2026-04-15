package Logic;

import Ui.ChatClientUI;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
public class ChatClientLogic {
    private final ChatClientUI ui;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String myUsername;
    private volatile boolean connected = false;

    public ChatClientLogic(ChatClientUI ui) {
        this.ui = ui;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public void connect(String ip, String portStr, String username) {
        if (ip.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            ui.showErr("Please fill in all connection fields.", "Missing Info");
            return;
        }
        myUsername = username;
        new Thread(() -> {
            try {
                int p = Integer.parseInt(portStr);
                socket = new Socket(ip, p);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                out.println("JOIN|" + myUsername);

                String first = in.readLine();
                if (first == null) {
                    ui.showErr("Server closed connection.", "Error");
                    socket.close();
                    return;
                }
                if (first.startsWith("ERROR|")) {
                    ui.showErr(first.substring(6), "Error");
                    socket.close();
                    return;
                }

                connected = true;
                SwingUtilities.invokeLater(() -> ui.setConnected(true));
                ui.addLog("Connected to " + ip + ":" + p + " as " + myUsername);
                handleLine(first);
                listenLoop();

            } catch (NumberFormatException e) {
                ui.showErr("Invalid port number.", "Error");
            } catch (IOException e) {
                ui.showErr("Connection failed:\n" + e.getMessage(), "Error");
            }
        }, "rx").start();
    }

    private void listenLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) handleLine(line);
        } catch (IOException e) {
            if (connected) ui.addLog("Connection lost: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> ui.setConnected(false));
    }

    public void disconnect() {
        connected = false;
        if (out != null) out.println("EXIT");
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        ui.addLog("Disconnected.");
        SwingUtilities.invokeLater(() -> ui.setConnected(false));
    }

    private void handleLine(String line) {
        if (line.startsWith("MSG|")) {
            String[] p = line.split("\\|", 4);
            if (p.length == 4) ui.addPublicBubble(p[1], p[2], p[3]);

        } else if (line.startsWith("PRIVATE|")) {
            String[] p = line.split("\\|", 4);
            if (p.length == 4) ui.addPrivateBubble("📩 " + p[1], p[2], p[3], false);

        } else if (line.startsWith("PRIVATE_SENT|")) {
            String[] p = line.split("\\|", 4);
            if (p.length == 4) ui.addPrivateBubble("📤 To " + p[1], p[2], p[3], true);

        } else if (line.startsWith("FILE|")) {
            String[] p = line.split("\\|", 5);
            if (p.length == 5) {
                ui.addFileBubble(p[1], p[2], p[3]);
                offerDownload(p[1], p[3], p[4]);
            }

        } else if (line.startsWith("USERLIST|")) {
            String body = line.substring(9);
            SwingUtilities.invokeLater(() -> {
                ui.clearUserList();
                if (!body.isEmpty())
                    Arrays.stream(body.split(",")).sorted().forEach(ui::addUser);
            });

        } else if (line.startsWith("LOG|")) {
            ui.addSysBubble(line.substring(4));
        } else if (line.startsWith("ERROR|")) {
            ui.addErrBubble(line.substring(6));
        } else if (line.startsWith("SERVER|")) {
            ui.addSysBubble("🔔 " + line.substring(7));
        }
    }

    public void sendMessage(String text) {
        if (!connected || text.isEmpty()) return;
        out.println("MSG|" + text);
    }

    public void sendFile(File file) {
        if (!connected) return;
        if (file.length() > 10L * 1024 * 1024) {
            ui.showErr("Max file size is 10 MB.", "Too Large");
            return;
        }
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                out.println("FILE|" + file.getName() + "|" + Base64.getEncoder().encodeToString(fis.readAllBytes()));
                ui.addLog("File sent: " + file.getName());
            } catch (IOException e) {
                ui.addLog("File error: " + e.getMessage());
            }
        }, "tx-file").start();
    }

    private void offerDownload(String from, String filename, String data) {
        if (from.equals(myUsername)) return;
        SwingUtilities.invokeLater(() -> {
            if (JOptionPane.showConfirmDialog(ui,
                    from + " shared: " + filename + "\n\nDownload it?",
                    "File", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(filename));
            if (fc.showSaveDialog(ui) != JFileChooser.APPROVE_OPTION) return;
            new Thread(() -> {
                try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                    fos.write(Base64.getDecoder().decode(data));
                    ui.addLog("Saved: " + fc.getSelectedFile().getAbsolutePath());
                } catch (IOException e) {
                    ui.addLog("Save error: " + e.getMessage());
                }
            }, "rx-file").start();
        });
    }
}
