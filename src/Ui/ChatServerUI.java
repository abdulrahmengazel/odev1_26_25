package Ui;

import Logic.ChatServerLogic;
import config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ChatServerUI extends JFrame {

    private final ChatServerLogic logic;

    // ── UI ───────────────────────────────────────────────────────────────────
    private JTextArea     logArea;
    private DefaultListModel<String> userListModel;
    private JTextField    portField;
    private JTextField    announcementField;
    private JLabel        statusLabel;
    private JButton       startBtn, stopBtn, announceBtn;

    public ChatServerUI() {
        this.logic = new ChatServerLogic(this);
        setTitle("TCP Chat Server");
        setSize(720, 540);
        setMinimumSize(new Dimension(580, 400));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (logic.isRunning()) logic.stopServer();
                dispose();
            }
        });
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        getContentPane().setBackground(Config.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // ─── TOP BAR ───
        JPanel topBar = new JPanel(new BorderLayout(0, 6));
        topBar.setBackground(Config.BG_PANEL);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Config.BG_INPUT));

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controlRow.setOpaque(false);

        controlRow.add(Config.styledLabel("Port:", Config.BOLD_FONT));
        portField = Config.styledTextField("5555", 6);
        controlRow.add(portField);

        startBtn = Config.styledButton("▶  Start Server", Config.SUCCESS);
        stopBtn  = Config.styledButton("■  Stop Server",  Config.DANGER);
        stopBtn.setEnabled(false);
        controlRow.add(startBtn);
        controlRow.add(stopBtn);

        statusLabel = Config.styledLabel("● Offline", Config.BOLD_FONT);
        statusLabel.setForeground(Config.DANGER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        controlRow.add(statusLabel);

        JPanel announcementRow = new JPanel(new BorderLayout(8, 0));
        announcementRow.setOpaque(false);
        announcementRow.add(Config.styledLabel("Announcement:", Config.BOLD_FONT), BorderLayout.WEST);
        announcementField = Config.styledTextField("", 22);
        announcementField.setEnabled(false);
        announcementRow.add(announcementField, BorderLayout.CENTER);

        announceBtn = Config.styledButton("Send", Config.ACCENT);
        announceBtn.setEnabled(false);
        announcementRow.add(announceBtn, BorderLayout.EAST);

        topBar.add(controlRow, BorderLayout.NORTH);
        topBar.add(announcementRow, BorderLayout.SOUTH);

        add(topBar, BorderLayout.NORTH);

        // ─── CENTER: LOG + USER LIST ───
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(Config.MONO_FONT);
        logArea.setBackground(Config.BG_DARK);
        logArea.setForeground(Config.FG_TEXT);
        logArea.setCaretColor(Config.FG_TEXT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(Config.titledBorder("Server Log"));
        logScroll.getViewport().setBackground(Config.BG_DARK);

        userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);
        userList.setBackground(Config.BG_PANEL);
        userList.setForeground(Config.FG_TEXT);
        userList.setFont(Config.UI_FONT);
        userList.setSelectionBackground(Config.ACCENT.darker());
        userList.setCellRenderer(new UserCellRenderer());
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(Config.titledBorder("Connected Users"));
        userScroll.setPreferredSize(new Dimension(160, 0));
        userScroll.getViewport().setBackground(Config.BG_PANEL);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScroll, userScroll);
        split.setResizeWeight(0.78);
        split.setDividerSize(4);
        split.setBackground(Config.BG_DARK);
        add(split, BorderLayout.CENTER);

        // ─── BOTTOM STATUS BAR ───
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        statusBar.setBackground(Config.BG_PANEL);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Config.BG_INPUT));
        JLabel hint = Config.styledLabel("Double-click a user to see their IP in log", null);
        hint.setForeground(Config.FG_DIM);
        hint.setFont(Config.SMALL_FONT.deriveFont(Font.ITALIC));
        statusBar.add(hint);
        add(statusBar, BorderLayout.SOUTH);

        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = userList.getSelectedValue();
                    if (sel != null) {
                        logic.logClientIp(sel);
                    }
                }
            }
        });

        // ─── LISTENERS ───
        startBtn.addActionListener(e -> logic.startServer(portField.getText().trim()));
        stopBtn .addActionListener(e -> logic.stopServer());
        announceBtn.addActionListener(e -> sendAnnouncementUI());
        announcementField.addActionListener(e -> sendAnnouncementUI());
    }

    private void sendAnnouncementUI() {
        String text = announcementField.getText();
        logic.sendAnnouncement(text);
        announcementField.setText("");
        announcementField.requestFocusInWindow();
    }

    // ── Public methods to update UI from logic ──────────────────────────────
    public void addLog(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + ts + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateServerStatus(boolean running, int port) {
        SwingUtilities.invokeLater(() -> {
            startBtn.setEnabled(!running);
            stopBtn .setEnabled(running);
            portField.setEnabled(!running);
            announcementField.setEnabled(running);
            announceBtn.setEnabled(running);
            statusLabel.setText(running ? "● Online :" + port : "● Offline");
            statusLabel.setForeground(running ? Config.SUCCESS : Config.DANGER);
            if (!running) announcementField.setText("");
        });
    }

    public void updateUserList(Set<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            users.stream().sorted().forEach(userListModel::addElement);
        });
    }

    public void showErrorMessage(String message, String title) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
    }


    // ── User list cell renderer ────────────────────────────────────────────────
    static class UserCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int idx, boolean sel, boolean focus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
            lbl.setText("  🟢  " + value);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            lbl.setForeground(sel ? Color.WHITE : Config.FG_TEXT);
            lbl.setBackground(sel ? new Color(Config.ACCENT.getRed(), Config.ACCENT.getGreen(), Config.ACCENT.getBlue(), 80) : Config.BG_PANEL);
            return lbl;
        }
    }
}