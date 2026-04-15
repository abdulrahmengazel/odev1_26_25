package Ui;

import Logic.ChatClientLogic;
import config.Config;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientUI extends JFrame {

    private final ChatClientLogic logic;

    // ═══════════════════════ UI FIELDS ══════════════════════════════════════
    private JTextField ipField, portField, usernameField;
    private JButton    connectBtn, disconnectBtn;
    private JLabel     statusLabel;

    private JPanel      chatPanel;   // BoxLayout Y – holds bubble rows
    private JScrollPane chatScroll;

    private JTextField  messageField;
    private JButton     sendBtn, fileBtn;

    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userList;
    private JTextArea     logArea;

    // @ autocomplete
    private JPopupMenu    mentionPopup;
    private JList<String> mentionList;
    private final DefaultListModel<String> mentionModel = new DefaultListModel<>();

    public ChatClientUI() {
        logic = new ChatClientLogic(this);
        setTitle("TCP Chat Client");
        setSize(960, 680);
        setMinimumSize(new Dimension(720, 520));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        buildMentionPopup();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (logic.isConnected()) logic.disconnect();
                dispose(); System.exit(0);
            }
        });
    }

    // ─────────────────────────────────── BUILD UI ───────────────────────────
    private void buildUI() {
        getContentPane().setBackground(Config.BG_APP);
        setLayout(new BorderLayout());
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildLogBar(),  BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 9));
        bar.setBackground(Config.BG_SIDE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Config.BG_INPUT));

        bar.add(Config.styledLabel("🖧  IP:", Config.BOLD_FONT));
        ipField = Config.styledTextField("localhost", 10); bar.add(ipField);
        bar.add(Config.styledLabel("Port:", Config.BOLD_FONT));
        portField = Config.styledTextField("5555", 5); bar.add(portField);
        bar.add(Config.styledLabel("Username:", Config.BOLD_FONT));
        usernameField = Config.styledTextField("", 10); bar.add(usernameField);

        connectBtn    = Config.styledButton("Connect",    Config.SUCCESS);
        disconnectBtn = Config.styledButton("Disconnect", Config.DANGER);
        disconnectBtn.setEnabled(false);
        bar.add(connectBtn); bar.add(disconnectBtn);

        statusLabel = Config.styledLabel("  ⬤ Disconnected", Config.BOLD_FONT);
        statusLabel.setForeground(Config.DANGER);
        bar.add(statusLabel);

        connectBtn   .addActionListener(e -> logic.connect(ipField.getText().trim(), portField.getText().trim(), usernameField.getText().trim()));
        disconnectBtn.addActionListener(e -> logic.disconnect());
        return bar;
    }

    private JComponent buildCenter() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Config.BG_APP);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));

        chatScroll = new JScrollPane(chatPanel);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setBackground(Config.BG_APP);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.getVerticalScrollBar().setUnitIncrement(20);

        messageField = Config.styledTextField("", 0);
        messageField.setEnabled(false);

        sendBtn = Config.styledButton("Send ➤", Config.ACCENT);
        fileBtn = Config.styledButton("📎 File", Config.GOLD);
        sendBtn.setEnabled(false);
        fileBtn.setEnabled(false);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBox.setBackground(Config.BG_PANEL);
        btnBox.add(fileBtn); btnBox.add(sendBtn);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(Config.BG_PANEL);
        inputRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Config.BG_INPUT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        inputRow.add(messageField, BorderLayout.CENTER);
        inputRow.add(btnBox,       BorderLayout.EAST);

        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(Config.BG_APP);
        chatArea.add(chatScroll, BorderLayout.CENTER);
        chatArea.add(inputRow,   BorderLayout.SOUTH);

        JPanel sidebar = buildSidebar();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatArea, sidebar);
        split.setResizeWeight(0.78);
        split.setDividerSize(3);
        split.setBackground(Config.BG_APP);

        sendBtn.addActionListener(e -> sendMessageUI());
        fileBtn.addActionListener(e -> sendFileUI());
        messageField.addActionListener(e -> sendMessageUI());
        return split;
    }

    private JPanel buildSidebar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Config.BG_SIDE);
        p.setPreferredSize(new Dimension(170, 0));

        JLabel title = Config.styledLabel("  Online Users", Config.BOLD_FONT);
        title.setForeground(Config.ACCENT);
        title.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Config.BG_INPUT),
                BorderFactory.createEmptyBorder(10, 6, 10, 6)));
        p.add(title, BorderLayout.NORTH);

        userList = new JList<>(userListModel);
        userList.setBackground(Config.BG_SIDE);
        userList.setForeground(Config.FG_TEXT);
        userList.setFont(Config.UI_FONT);
        userList.setCellRenderer(new UserCellRenderer());
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = userList.getSelectedValue();
                    if (sel != null && !sel.equals(logic.getMyUsername())) {
                        messageField.setText("@" + sel + " ");
                        messageField.setCaretPosition(messageField.getText().length());
                        messageField.requestFocus();
                    }
                }
            }
        });

        JScrollPane sc = new JScrollPane(userList);
        sc.setBorder(null);
        sc.getViewport().setBackground(Config.BG_SIDE);
        p.add(sc, BorderLayout.CENTER);

        JLabel hint = Config.styledLabel("  ⓘ Dbl-click to @mention", Config.SMALL_FONT.deriveFont(Font.ITALIC));
        hint.setForeground(Config.FG_DIM);
        hint.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildLogBar() {
        logArea = new JTextArea(4, 0);
        logArea.setEditable(false);
        logArea.setFont(Config.MONO_FONT);
        logArea.setBackground(new Color(13, 14, 20));
        logArea.setForeground(new Color(100, 110, 135));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JScrollPane sc = new JScrollPane(logArea);
        sc.setPreferredSize(new Dimension(0, 100));
        sc.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Config.BG_INPUT));
        sc.getViewport().setBackground(new Color(13, 14, 20));

        JLabel lbl = Config.styledLabel("  Log", Config.SMALL_FONT.deriveFont(Font.BOLD));
        lbl.setForeground(Config.FG_DIM);
        lbl.setBackground(new Color(13, 14, 20));
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 6, 0, 0));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(new Color(13, 14, 20));
        wrap.add(lbl, BorderLayout.NORTH);
        wrap.add(sc,  BorderLayout.CENTER);
        return wrap;
    }

    // ─────────────────────────────── @ AUTOCOMPLETE ─────────────────────────
    private void buildMentionPopup() {
        mentionList = new JList<>(mentionModel);
        mentionList.setBackground(Config.BG_PANEL);
        mentionList.setForeground(Config.FG_TEXT);
        mentionList.setFont(Config.BOLD_FONT);
        mentionList.setSelectionBackground(new Color(0, 90, 180));
        mentionList.setSelectionForeground(Color.WHITE);
        mentionList.setCellRenderer(new MentionCellRenderer());
        mentionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mentionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { completeMention(); }
        });

        JScrollPane sc = new JScrollPane(mentionList);
        sc.setBorder(null);
        sc.setPreferredSize(new Dimension(190, 140));

        mentionPopup = new JPopupMenu();
        mentionPopup.setBorder(BorderFactory.createLineBorder(Config.ACCENT.darker(), 1));
        mentionPopup.setBackground(Config.BG_PANEL);
        mentionPopup.add(sc);

        messageField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(() -> checkMention()); }
            public void removeUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(() -> checkMention()); }
            public void changedUpdate(DocumentEvent e) {}
        });

        messageField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!mentionPopup.isVisible()) return;
                int idx = mentionList.getSelectedIndex();
                int sz  = mentionModel.getSize();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN   -> { mentionList.setSelectedIndex(Math.min(idx+1, sz-1)); e.consume(); }
                    case KeyEvent.VK_UP     -> { mentionList.setSelectedIndex(Math.max(idx-1, 0));    e.consume(); }
                    case KeyEvent.VK_ENTER  -> { if (idx >= 0) { completeMention(); e.consume(); } }
                    case KeyEvent.VK_ESCAPE -> mentionPopup.setVisible(false);
                }
            }
        });
    }

    private void checkMention() {
        String text  = messageField.getText();
        int    caret = Math.min(messageField.getCaretPosition(), text.length());

        int atIdx = -1;
        for (int i = caret - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '@') { atIdx = i; break; }
            if (c == ' ') break;
        }
        if (atIdx < 0) { mentionPopup.setVisible(false); return; }

        String query = text.substring(atIdx + 1, caret).toLowerCase();
        mentionModel.clear();
        for (int i = 0; i < userListModel.getSize(); i++) {
            String u = userListModel.getElementAt(i);
            if (!u.equals(logic.getMyUsername()) && u.toLowerCase().startsWith(query))
                mentionModel.addElement(u);
        }

        if (mentionModel.isEmpty()) { mentionPopup.setVisible(false); return; }

        mentionList.setSelectedIndex(0);
        int popH = Math.min(mentionModel.getSize() * 38 + 8, 160);
        mentionPopup.getComponent(0).setPreferredSize(new Dimension(200, popH));

        if (!mentionPopup.isVisible())
            mentionPopup.show(messageField, 8, -popH - 4);
    }

    private void completeMention() {
        String sel = mentionList.getSelectedValue();
        if (sel == null) return;

        String text  = messageField.getText();
        int    caret = Math.min(messageField.getCaretPosition(), text.length());

        int atIdx = -1;
        for (int i = caret - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '@') { atIdx = i; break; }
            if (c == ' ') break;
        }
        if (atIdx >= 0) {
            String newText = text.substring(0, atIdx) + "@" + sel + " " + text.substring(caret);
            messageField.setText(newText);
            messageField.setCaretPosition(atIdx + sel.length() + 2);
        }
        mentionPopup.setVisible(false);
        messageField.requestFocus();
    }

    // ─────────────────────────────── UI ACTIONS ─────────────────────────────
    public void setConnected(boolean on) {
        connectBtn.setEnabled(!on); disconnectBtn.setEnabled(on);
        sendBtn.setEnabled(on);     fileBtn.setEnabled(on);
        messageField.setEnabled(on);
        ipField.setEnabled(!on); portField.setEnabled(!on); usernameField.setEnabled(!on);
        statusLabel.setText(on ? "  ⬤ " + logic.getMyUsername() : "  ⬤ Disconnected");
        statusLabel.setForeground(on ? Config.SUCCESS : Config.DANGER);
        if (!on) { userListModel.clear(); mentionPopup.setVisible(false); }
    }

    private void sendMessageUI() {
        String text = messageField.getText().trim();
        logic.sendMessage(text);
        mentionPopup.setVisible(false);
        messageField.setText("");
        messageField.requestFocus();
    }

    private void sendFileUI() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        logic.sendFile(fc.getSelectedFile());
    }

    // ─────────────────────────────── PUBLIC LOGIC CALLBACKS ─────────────────
    public void showErr(String msg, String title) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE));
    }

    public void addLog(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + ts + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void clearUserList() {
        userListModel.clear();
    }

    public void addUser(String username) {
        userListModel.addElement(username);
    }

    public void addPublicBubble(String sender, String time, String body) {
        boolean own = sender.equals(logic.getMyUsername());
        SwingUtilities.invokeLater(() ->
                appendRow(makeBubble(own ? null : sender, time, body,
                        own ? Config.BG_BUBBLE_R : Config.BG_BUBBLE_L,
                        own ? Color.WHITE  : Config.FG_TEXT, own, own ? Config.ACCENT : senderColor(sender))));
    }

    public void addPrivateBubble(String label, String time, String body, boolean sent) {
        SwingUtilities.invokeLater(() ->
                appendRow(makeBubble(label, time, body, Config.BG_BUBBLE_P,
                        new Color(230, 210, 255), sent, Config.PURPLE)));
    }

    public void addFileBubble(String sender, String time, String filename) {
        boolean own = sender.equals(logic.getMyUsername());
        SwingUtilities.invokeLater(() ->
                appendRow(makeBubble(own ? null : sender, time, "📎  " + filename,
                        Config.BG_BUBBLE_F, Config.GOLD, own, Config.GOLD)));
    }

    public void addSysBubble(String msg) {
        SwingUtilities.invokeLater(() -> {
            JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
            lbl.setFont(Config.SMALL_FONT.deriveFont(Font.ITALIC));
            lbl.setForeground(Config.FG_DIM);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            row.add(lbl);
            appendRow(row);
        });
    }

    public void addErrBubble(String msg) {
        SwingUtilities.invokeLater(() -> {
            JLabel lbl = new JLabel("⚠  " + msg, SwingConstants.CENTER);
            lbl.setFont(Config.SMALL_FONT);
            lbl.setForeground(Config.DANGER);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            row.add(lbl);
            appendRow(row);
        });
    }

    // ─────────────────────────────── BUBBLE RENDERING ───────────────────────
    private JPanel makeBubble(String senderLabel, String time,
                              String body, Color bg, Color fg,
                              boolean alignRight, Color nameColor) {

        String html = "<html><div style='width:290px;font-family:Segoe UI;"
                + "font-size:12pt;color:" + hex(fg) + "'>"
                + escHtml(body) + "</div></html>";
        JLabel textLbl = new JLabel(html);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLbl.setForeground(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 150));

        JPanel timeRow = new JPanel(new FlowLayout(alignRight ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        timeRow.setOpaque(false);
        timeRow.add(timeLbl);

        final Color bubbleBg = bg;
        final boolean right  = alignRight;
        JPanel bubble = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bubbleBg);
                int arc = 18, w = getWidth(), h = getHeight();
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                if (right) g2.fillRect(w - arc/2, h - arc/2, arc/2, arc/2);
                else       g2.fillRect(0,          h - arc/2, arc/2, arc/2);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        bubble.add(textLbl,  BorderLayout.CENTER);
        bubble.add(timeRow,  BorderLayout.SOUTH);
        bubble.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        JPanel nameChip = null;
        if (senderLabel != null) {
            JLabel nameLbl = new JLabel(senderLabel);
            nameLbl.setFont(Config.SMALL_FONT.deriveFont(Font.BOLD));
            nameLbl.setForeground(nameColor != null ? nameColor : Config.FG_DIM);

            nameChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            nameChip.setOpaque(false);
            nameChip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            nameChip.add(nameLbl);
        }

        JPanel hRow = new JPanel();
        hRow.setLayout(new BoxLayout(hRow, BoxLayout.X_AXIS));
        hRow.setOpaque(false);
        hRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        int margin = 14;
        if (alignRight) {
            hRow.add(Box.createHorizontalGlue());
            hRow.add(bubble);
            hRow.add(Box.createRigidArea(new Dimension(margin, 0)));
        } else {
            hRow.add(Box.createRigidArea(new Dimension(margin, 0)));
            hRow.add(bubble);
            hRow.add(Box.createHorizontalGlue());
        }

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        if (nameChip != null) row.add(nameChip);
        row.add(hRow);
        return row;
    }

    private void appendRow(JPanel row) {
        chatPanel.add(row);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        chatPanel.revalidate();
        chatPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = chatScroll.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }

    // ─────────────────────────────── HELPERS ────────────────────────────────
    private Color senderColor(String name) {
        Color[] pal = {
                new Color(100, 180, 255), new Color(100, 220, 150),
                new Color(190, 140, 255), new Color(255, 180,  80),
                new Color(255, 120, 120), new Color( 80, 215, 215)
        };
        return pal[Math.abs(name.hashCode()) % pal.length];
    }

    private static String escHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\n","<br>");
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }


    // ─────────────────────────── CELL RENDERERS ─────────────────────────────
    static class UserCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx, boolean sel, boolean focus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
            l.setText("  🟢  " + value);
            l.setFont(Config.UI_FONT);
            l.setBorder(BorderFactory.createEmptyBorder(7, 4, 7, 4));
            l.setForeground(sel ? Color.WHITE : new Color(210, 215, 232));
            l.setBackground(sel ? new Color(0, 90, 200) : Config.BG_SIDE);
            return l;
        }
    }

    class MentionCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx, boolean sel, boolean focus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
            String name = value.toString();
            Color nc = senderColor(name);
            l.setText("  @" + name);
            l.setFont(Config.BOLD_FONT);
            l.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            l.setForeground(sel ? Color.WHITE : nc);
            l.setBackground(sel ? nc.darker().darker() : Config.BG_PANEL);
            return l;
        }
    }
}