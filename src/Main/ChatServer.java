package Main;

import Ui.ChatServerUI;

import javax.swing.*;

public class ChatServer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new ChatServerUI().setVisible(true);
        });
    }
}
