package Main;

import Ui.ChatClientUI;
import javax.swing.*;

public class ChatClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new ChatClientUI().setVisible(true);
        });
    }
}
