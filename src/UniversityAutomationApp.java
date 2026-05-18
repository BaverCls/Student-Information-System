import models.*;
import ui.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatClientProperties;

public class UniversityAutomationApp extends JFrame {
    private static final Color DARK_BG = new Color(15, 23, 42); // Deep dark blue
    private static final Color PANEL_BG = new Color(30, 41, 59); // Slightly lighter dark
    private static final Color PANEL_BG_LIGHT = new Color(51, 65, 85); // Borders and highlights
    private static final Color TEXT_LIGHT = new Color(248, 250, 252); // White/Light gray text
    private static final Color TEXT_MUTED = new Color(148, 163, 184); // Muted text
    private static final Color ACCENT = new Color(56, 189, 248); // Bright blue accent
    private static final Color ACCENT_HOVER = new Color(14, 165, 233);
    private static final Color TOP_BAR_BG = new Color(15, 23, 42);
    private static final Font APP_FONT = loadAppFont();
    
    private DataStore data = new DataStore();
    private User currentUser = null;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel dashboardPanel;

    public UniversityAutomationApp() {
        data.loadData();
        
        setTitle("Student Information System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createLoginPanel();
        add(mainPanel);
        setVisible(true);
    }

    private void createLoginPanel() {
        // ── Outer background panel ──────────────────────────────────────────
        JPanel loginPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(13, 17, 30));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        loginPanel.setOpaque(true);
        loginPanel.setBackground(new Color(13, 17, 30));

        // ── Card: custom-painted rounded rectangle ───────────────────────────
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(4, 6, getWidth() - 4, getHeight() - 4, 24, 24);
                // Card background
                g2.setColor(new Color(28, 35, 52));
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 24, 24);
                // Card border
                g2.setColor(new Color(55, 70, 100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 24, 24);
                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(460, 390));

        // Shared GridBagConstraints: every row fills full width
        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx   = 0;
        fc.fill    = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("System Login", SwingConstants.CENTER);
        title.setFont(APP_FONT.deriveFont(Font.BOLD, 26f));
        title.setForeground(new Color(248, 250, 252));

        // ── Username ─────────────────────────────────────────────────────────
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(APP_FONT.deriveFont(Font.PLAIN, 13f));
        userLabel.setForeground(new Color(148, 163, 184));

        JTextField usernameField = new JTextField("admin");
        styleLoginField(usernameField);

        // ── Password ─────────────────────────────────────────────────────────
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(APP_FONT.deriveFont(Font.PLAIN, 13f));
        passLabel.setForeground(new Color(148, 163, 184));

        JPasswordField passwordField = new JPasswordField("123");
        styleLoginField(passwordField);

        // ── Login Button: fully custom-painted pill ───────────────────────────
        final Color BTN_NORMAL = new Color(0, 210, 255);
        final Color BTN_HOVER  = new Color(0, 180, 225);
        final Color BTN_PRESS  = new Color(0, 150, 200);
        final boolean[] btnState = {false, false}; // [hover, pressed]

        JButton loginButton = new JButton("Login") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = btnState[1] ? BTN_PRESS : btnState[0] ? BTN_HOVER : BTN_NORMAL;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setFont(APP_FONT.deriveFont(Font.BOLD, 15f));
                g2.setColor(new Color(10, 20, 40));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize()  { return new Dimension(10, 52); }
            @Override public Dimension getMinimumSize()     { return new Dimension(10, 52); }
        };
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e)  { btnState[0] = true;  loginButton.repaint(); }
            public void mouseExited(MouseEvent e)   { btnState[0] = false; btnState[1] = false; loginButton.repaint(); }
            public void mousePressed(MouseEvent e)  { btnState[1] = true;  loginButton.repaint(); }
            public void mouseReleased(MouseEvent e) { btnState[1] = false; loginButton.repaint(); }
        });

        loginButton.addActionListener(e -> {
            String uName = usernameField.getText().trim();
            String pass  = new String(passwordField.getPassword());
            User found = null;
            for (User u : data.users) {
                if (u.getUsername().equals(uName) && u.getPassword().equals(pass)) {
                    found = u; break;
                }
            }
            if (found != null) {
                currentUser = found;
                refreshDashboard();
                cardLayout.show(mainPanel, "dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login!", "Warning", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ── Assemble card with GridBagLayout (every row = full width) ─────────
        fc.gridy = 0; fc.insets = new Insets(44, 44, 28, 44); card.add(title,         fc);
        fc.gridy = 1; fc.insets = new Insets(0,  44,  6, 44); card.add(userLabel,     fc);
        fc.gridy = 2; fc.insets = new Insets(0,  44, 16, 44); card.add(usernameField, fc);
        fc.gridy = 3; fc.insets = new Insets(0,  44,  6, 44); card.add(passLabel,     fc);
        fc.gridy = 4; fc.insets = new Insets(0,  44, 24, 44); card.add(passwordField, fc);
        fc.gridy = 5; fc.insets = new Insets(0,  44, 44, 44); card.add(loginButton,   fc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(card, gbc);

        mainPanel.add(loginPanel, "login");
    }

    private void styleLoginField(JTextField f) {
        f.setFont(APP_FONT.deriveFont(14f));
        f.setBackground(new Color(18, 24, 40));
        f.setForeground(new Color(185, 200, 220));
        f.setCaretColor(new Color(0, 210, 255));
        f.putClientProperty(FlatClientProperties.STYLE, "background: #12182B; foreground: #B9C8DC; arc: 10;");
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(55, 70, 105), 10),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
    }

    private void refreshDashboard() {
        if (dashboardPanel != null) {
            mainPanel.remove(dashboardPanel);
        }

        dashboardPanel = createDashboard();
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.revalidate();
        mainPanel.repaint();

        if (currentUser != null) {
            cardLayout.show(mainPanel, "dashboard");
        }
    }

    private JPanel createDashboard() {
        if (currentUser.getRole().equals("ADMIN")) {
            return new AdminUI(data, currentUser, () -> {
                currentUser = null;
                cardLayout.show(mainPanel, "login");
            }, this::refreshDashboard);
        } else if (currentUser.getRole().equals("INSTRUCTOR")) {
            return new InstructorUI(data, currentUser, () -> {
                currentUser = null;
                cardLayout.show(mainPanel, "login");
            });
        } else {
            return new StudentUI(data, currentUser, () -> {
                currentUser = null;
                cardLayout.show(mainPanel, "login");
            });
        }
    }

    private static Font loadAppFont() {
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String fontName : graphics.getAvailableFontFamilyNames()) {
            if (fontName.equalsIgnoreCase("Inter")) {
                return new Font(fontName, Font.PLAIN, 15);
            }
        }
        return new Font("Segoe UI", Font.PLAIN, 15);
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        public RoundedBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    private static class GradientPanel extends JPanel {
        public GradientPanel(LayoutManager layout) { super(layout); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            Color color1 = DARK_BG;
            Color color2 = new Color(24, 32, 45);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("ScrollBar.thumbArc", 10);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabSeparatorsFullHeight", true);
            UIManager.put("TitlePane.unifiedBackground", false);
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(() -> new UniversityAutomationApp());
    }
}
