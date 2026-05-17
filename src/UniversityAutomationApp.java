import models.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
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
    private List<String> draftEnrollments = new ArrayList<>();

    public UniversityAutomationApp() {
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
        JLabel userLabel = new JLabel("Kullanıcı Adı");
        userLabel.setFont(APP_FONT.deriveFont(Font.PLAIN, 13f));
        userLabel.setForeground(new Color(148, 163, 184));

        JTextField usernameField = new JTextField("admin");
        styleLoginField(usernameField);

        // ── Password ─────────────────────────────────────────────────────────
        JLabel passLabel = new JLabel("Şifre");
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

    /** Styles an input field for the login card. */
    private void styleLoginField(JTextField f) {
        f.setFont(APP_FONT.deriveFont(14f));
        f.setBackground(new Color(18, 24, 40));
        f.setForeground(new Color(185, 200, 220));
        f.setCaretColor(new Color(0, 210, 255));
        // Let GridBagLayout control width; only fix height via border padding
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
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(DARK_BG);
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(TOP_BAR_BG);
        topBar.setPreferredSize(new Dimension(1000, 50));
        
        String welcomeText = "Welcome: " + currentUser.getFullName() + " [" + currentUser.getRole() + "]";
        if (currentUser.getRole().equals("STUDENT")) {
            StudentProfile sp = findStudentProfile(currentUser.getUsername());
            if (sp != null) {
                welcomeText += " - Year " + sp.getYear();
            }
        }
        JLabel welcomeLabel = new JLabel("  " + welcomeText);
        welcomeLabel.setForeground(TEXT_LIGHT);
        welcomeLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 16f));
        
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton);
        logoutButton.setBackground(new Color(225, 29, 72)); // Modern soft red

        logoutButton.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "login");
        });

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutButton);

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutPanel, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(APP_FONT);
        tabs.setBackground(DARK_BG);
        tabs.setForeground(TEXT_LIGHT);

        if (currentUser.getRole().equals("ADMIN")) {
            tabs.addTab("User Management", createTablePanel("USER"));
            tabs.addTab("Student Management", createTablePanel("STUDENT"));
            tabs.addTab("Course Management", createTablePanel("COURSE"));
            tabs.addTab("Curriculum Management", createAdminCurriculumPanel());
            tabs.addTab("Reports", createReportPanel());
            tabs.addTab("System Config", createSystemConfigPanel());
        } else if (currentUser.getRole().equals("INSTRUCTOR")) {
            tabs.addTab("My Courses", createTablePanel("INSTRUCTOR_COURSES"));
            tabs.addTab("Grade Entry", createGradeEntryPanel());
            tabs.addTab("Statistics", createInstructorStatsPanel());
        } else {
            tabs.addTab("Course Registration", createEnrollmentPanel());
            tabs.addTab("Grades", createGradesPanel());
            tabs.addTab("Curriculum", createCurriculumView());
            tabs.addTab("Transcript", createTranscriptView());
        }

        dashboard.add(topBar, BorderLayout.NORTH);
        dashboard.add(tabs, BorderLayout.CENTER);
        applyTheme(dashboard);
        return dashboard;
    }

    private JPanel createInstructorStatsPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(DARK_BG);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Top Filter Bar ──────────────────────────────────────────────────
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        top.setBackground(DARK_BG);

        // Course combo
        JLabel courseLabel = new JLabel("Course:");
        courseLabel.setFont(APP_FONT.deriveFont(Font.BOLD));
        courseLabel.setForeground(TEXT_LIGHT);
        JComboBox<String> courseCombo = new JComboBox<>();
        for (Course c : data.courses) {
            if (c.getInstructorUsername().equals(currentUser.getUsername())) {
                courseCombo.addItem(c.getCourseCode() + " - " + c.getCourseName());
            }
        }

        // Academic-year combo — populated from existing GradeRecords
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(APP_FONT.deriveFont(Font.BOLD));
        yearLabel.setForeground(TEXT_LIGHT);
        JComboBox<String> yearCombo = new JComboBox<>();
        yearCombo.addItem("All Years");
        java.util.TreeSet<String> years = new java.util.TreeSet<>();
        for (GradeRecord gr : data.grades) years.add(gr.getAcademicYear());
        for (String y : years) yearCombo.addItem(y);

        // Semester combo
        JLabel semLabel = new JLabel("Semester:");
        semLabel.setFont(APP_FONT.deriveFont(Font.BOLD));
        semLabel.setForeground(TEXT_LIGHT);
        JComboBox<String> semCombo = new JComboBox<>(new String[]{"All Semesters", "Fall", "Spring"});

        top.add(courseLabel); top.add(courseCombo);
        top.add(Box.createHorizontalStrut(10));
        top.add(yearLabel);   top.add(yearCombo);
        top.add(Box.createHorizontalStrut(10));
        top.add(semLabel);    top.add(semCombo);
        main.add(top, BorderLayout.NORTH);

        // ── Center: Summary Cards + Bar Chart ───────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(DARK_BG);
        center.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(DARK_BG);

        GradeChartPanel chartPanel = new GradeChartPanel();

        // Shared refresh runnable — reads all three combos
        Runnable refresh = () -> {
            String sel = (String) courseCombo.getSelectedItem();
            if (sel == null) return;
            String code    = sel.split(" - ")[0];
            String year    = (String) yearCombo.getSelectedItem();
            String sem     = (String) semCombo.getSelectedItem();
            updateInstructorStats(cardsPanel, chartPanel, code,
                    "All Years".equals(year) ? null : year,
                    "All Semesters".equals(sem) ? null : sem);
        };

        courseCombo.addActionListener(e -> refresh.run());
        yearCombo  .addActionListener(e -> refresh.run());
        semCombo   .addActionListener(e -> refresh.run());

        center.add(cardsPanel, BorderLayout.NORTH);
        center.add(chartPanel, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);

        // Initial update
        if (courseCombo.getItemCount() > 0) {
            refresh.run();
        } else {
            cardsPanel.add(new JLabel("No courses assigned."));
        }

        return main;
    }

    private JPanel createAdminCurriculumPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(DARK_BG);
        JLabel lbl = new JLabel("Select Department: ");
        lbl.setForeground(TEXT_LIGHT);
        lbl.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
        JComboBox<String> deptCombo = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
        topPanel.add(lbl);
        topPanel.add(deptCombo);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new BorderLayout());
        gridPanel.setBackground(DARK_BG);
        panel.add(gridPanel, BorderLayout.CENTER);

        Runnable refreshCurriculumGrid = () -> {
            gridPanel.removeAll();
            JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
            grid.setBackground(DARK_BG);
            
            String dept = (String) deptCombo.getSelectedItem();
            List<Curriculum> currList = data.getCurriculumForDept(dept);

            for (int sem = 1; sem <= 8; sem++) {
                int finalSem = sem;
                JPanel semPanel = new JPanel(new BorderLayout());
                semPanel.setBackground(PANEL_BG);
                semPanel.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(PANEL_BG_LIGHT, 12),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                JLabel semLabel = new JLabel("Semester " + sem);
                semLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
                semLabel.setForeground(ACCENT);
                semPanel.add(semLabel, BorderLayout.NORTH);

                JPanel coursesBox = new JPanel();
                coursesBox.setLayout(new BoxLayout(coursesBox, BoxLayout.Y_AXIS));
                coursesBox.setBackground(PANEL_BG);

                int totalECTS = 0;
                for (Curriculum c : currList) {
                    if (c.getSemester() == sem) {
                        Course course = findCourse(c.getCourseCode());
                        if (course != null) {
                            totalECTS += Integer.parseInt(course.getCredit());
                            JPanel courseRow = new JPanel(new BorderLayout(5, 5));
                            courseRow.setBackground(PANEL_BG);
                            courseRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
                            
                            JLabel cl = new JLabel("<html><b>" + course.getCourseCode() + "</b> - " + course.getCourseName() + " (" + course.getCredit() + " ECTS)</html>");
                            cl.setForeground(TEXT_LIGHT);
                            cl.setFont(APP_FONT.deriveFont(11f));
                            
                            JButton btnRemove = new JButton("X");
                            btnRemove.setFont(APP_FONT.deriveFont(Font.BOLD, 10f));
                            btnRemove.setBackground(new Color(239, 68, 68));
                            btnRemove.setForeground(Color.WHITE);
                            btnRemove.setMargin(new Insets(2, 5, 2, 5));
                            btnRemove.setFocusPainted(false);
                            btnRemove.addActionListener(e -> {
                                data.curriculums.remove(c);
                                data.saveData();
                                deptCombo.getActionListeners()[0].actionPerformed(null);
                            });
                            
                            courseRow.add(cl, BorderLayout.CENTER);
                            courseRow.add(btnRemove, BorderLayout.EAST);
                            coursesBox.add(courseRow);
                            coursesBox.add(Box.createRigidArea(new Dimension(0, 5)));
                        }
                    }
                }

                JScrollPane scroll = new JScrollPane(coursesBox);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                semPanel.add(scroll, BorderLayout.CENTER);

                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setBackground(PANEL_BG);
                
                JLabel ectsLabel = new JLabel("Total: " + totalECTS + " ECTS");
                ectsLabel.setFont(APP_FONT.deriveFont(Font.ITALIC, 11f));
                ectsLabel.setForeground(TEXT_MUTED);
                bottomPanel.add(ectsLabel, BorderLayout.WEST);

                JButton btnAdd = new JButton("Add Course");
                styleButton(btnAdd);
                btnAdd.setFont(APP_FONT.deriveFont(11f));
                btnAdd.setMargin(new Insets(2, 5, 2, 5));
                btnAdd.addActionListener(e -> showAddCourseDialog(dept, finalSem, currList, () -> deptCombo.getActionListeners()[0].actionPerformed(null)));
                bottomPanel.add(btnAdd, BorderLayout.EAST);
                
                semPanel.add(bottomPanel, BorderLayout.SOUTH);
                grid.add(semPanel);
            }
            gridPanel.add(grid, BorderLayout.CENTER);
            gridPanel.revalidate();
            gridPanel.repaint();
        };

        deptCombo.addActionListener(e -> refreshCurriculumGrid.run());
        refreshCurriculumGrid.run();

        return panel;
    }

    private void showAddCourseDialog(String dept, int semester, List<Curriculum> currentCurriculum, Runnable onAdd) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Course to " + dept + " Semester " + semester, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(DARK_BG);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(DARK_BG);
        JTextField searchField = new JTextField(20);
        styleTextField(searchField);
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        main.add(searchPanel, BorderLayout.NORTH);

        DefaultTableModel poolModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        poolModel.setColumnIdentifiers(new String[]{"Code", "Course Name", "Credit", "Dept", "Action"});
        JTable poolTable = new JTable(poolModel);
        styleTable(poolTable);
        TableRowSorter<DefaultTableModel> poolSorter = new TableRowSorter<>(poolModel);
        poolTable.setRowSorter(poolSorter);

        for (Course c : data.courses) {
            boolean exists = currentCurriculum.stream().anyMatch(curr -> curr.getCourseCode().equals(c.getCourseCode()));
            if (!exists) {
                poolModel.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getDepartment(), "Add"});
            }
        }

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) poolSorter.setRowFilter(null);
                else poolSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        poolTable.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JButton b = new JButton("Add");
                styleSmallButton(b);
                b.setBackground(new Color(34, 197, 94));
                JPanel p = new JPanel(new GridBagLayout());
                p.setOpaque(true);
                p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                p.add(b);
                return p;
            }
        });
        poolTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton b = new JButton("Add");
            private JPanel p;
            private String code;
            {
                styleSmallButton(b);
                b.setBackground(new Color(34, 197, 94));
                b.addActionListener(e -> {
                    stopCellEditing();
                    data.curriculums.add(new Curriculum(dept, semester, code));
                    data.saveData();
                    dialog.dispose();
                    onAdd.run();
                });
                p = new JPanel(new GridBagLayout());
                p.setOpaque(true);
                p.add(b);
            }
            @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                code = table.getValueAt(row, 0).toString();
                p.setBackground(table.getSelectionBackground());
                return p;
            }
            @Override public Object getCellEditorValue() { return "Add"; }
        });

        main.add(new JScrollPane(poolTable), BorderLayout.CENTER);
        dialog.add(main);
        applyTheme(main);
        dialog.setVisible(true);
    }

    /**
     * @param academicYear null = all years
     * @param semester     null = all semesters
     */
    private void updateInstructorStats(JPanel cardsPanel, GradeChartPanel chartPanel,
                                       String courseCode, String academicYear, String semester) {
        cardsPanel.removeAll();

        // Enrolled count — filtered by year+semester if specified
        int enrolled = 0;
        for (Enrollment e : data.enrollments) {
            if (!e.getCourseCode().equals(courseCode)) continue;
            if (academicYear != null && !e.getAcademicYear().equals(academicYear)) continue;
            if (semester    != null && !e.getSemester().equalsIgnoreCase(semester))  continue;
            enrolled++;
        }

        double sum = 0;
        int gradeCount = 0;
        int passed = 0;
        Map<String, Integer> distribution = new java.util.TreeMap<>();
        String[] gradesList = {"AA", "BA", "BB", "CB", "CC", "DC", "DD", "FF"};
        for (String g : gradesList) distribution.put(g, 0);

        for (GradeRecord gr : data.grades) {
            if (!gr.getCourseCode().equals(courseCode)) continue;
            if (academicYear != null && !gr.getAcademicYear().equals(academicYear)) continue;
            if (semester     != null && !gr.getSemester().equalsIgnoreCase(semester))  continue;

            double avg = calculateAverage(gr.getMidterm(), gr.getFinalExam());
            String letter = calculateLetterGrade(avg);
            distribution.put(letter, distribution.get(letter) + 1);
            sum += avg;
            gradeCount++;
            if (!letter.equals("FF") && !letter.equals("FD")) passed++;
        }

        double courseAvg    = gradeCount > 0 ? sum / gradeCount       : 0;
        double successRate  = gradeCount > 0 ? (passed * 100.0 / gradeCount) : 0;

        // Filter label shown in the subtitle of each card
        String filterInfo = (academicYear != null ? academicYear : "All Years")
                          + "  •  " + (semester != null ? semester : "All Semesters");
        chartPanel.setSubtitle(filterInfo);

        cardsPanel.add(createReportCard("TOTAL ENROLLED",  String.valueOf(enrolled), ACCENT));
        cardsPanel.add(createReportCard("AVERAGE SCORE",   String.format("%.1f", courseAvg),   new Color(16, 185, 129)));
        cardsPanel.add(createReportCard("SUCCESS RATE",    String.format("%.1f%%", successRate), new Color(245, 158, 11)));

        chartPanel.setDistribution(distribution);

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private static class GradeChartPanel extends JPanel {
        private Map<String, Integer> distribution = new java.util.HashMap<>();
        private String subtitle = "";

        public GradeChartPanel() {
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(PANEL_BG_LIGHT, 20),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
            ));
        }

        public void setDistribution(Map<String, Integer> dist) {
            this.distribution = dist;
            repaint();
        }

        public void setSubtitle(String text) {
            this.subtitle = text;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int topPad    = 50;  // extra room for the subtitle
            int sidePad   = 60;
            int bottomPad = 60;
            int chartW = w - sidePad * 2;
            int chartH = h - topPad - bottomPad;

            // ── Subtitle (filter info) ──────────────────────────────────────
            if (subtitle != null && !subtitle.isEmpty()) {
                g2.setFont(APP_FONT.deriveFont(Font.ITALIC, 12f));
                g2.setColor(TEXT_MUTED);
                int sw = g2.getFontMetrics().stringWidth(subtitle);
                g2.drawString(subtitle, (w - sw) / 2, 22);
            }

            // ── Chart title ─────────────────────────────────────────────────
            g2.setFont(APP_FONT.deriveFont(Font.BOLD, 13f));
            g2.setColor(TEXT_LIGHT);
            String title = "Grade Distribution";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (w - tw) / 2, 38);

            String[] keys = {"AA", "BA", "BB", "CB", "CC", "DC", "DD", "FF"};
            int maxVal = 1;
            for (int val : distribution.values()) if (val > maxVal) maxVal = val;

            int barW    = chartW / keys.length - 20;
            int startX  = sidePad + 10;
            int baselineY = topPad + chartH;

            // Y-axis baseline
            g2.setColor(TEXT_MUTED);
            g2.drawLine(sidePad, baselineY, w - sidePad, baselineY);

            // Subtle Y grid lines
            g2.setColor(new Color(71, 85, 105, 80));
            for (int tick = 1; tick <= 4; tick++) {
                int gy = baselineY - (int) (chartH * tick / 4.0);
                g2.drawLine(sidePad, gy, w - sidePad, gy);
            }

            Color failColor = new Color(239, 68, 68);

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                int val = distribution.getOrDefault(key, 0);
                int barH = (int) ((val / (double) maxVal) * chartH);

                int x = startX + i * (barW + 20);
                int y = baselineY - barH;

                Color barTop   = key.equals("FF") ? failColor : ACCENT;
                Color barBottom = new Color(barTop.getRed(), barTop.getGreen(), barTop.getBlue(), 80);
                GradientPaint gp = new GradientPaint(x, y, barTop, x, baselineY, barBottom);
                g2.setPaint(gp);
                if (barH > 0) g2.fillRoundRect(x, y, barW, barH, 10, 10);

                if (val > 0) {
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(APP_FONT.deriveFont(Font.BOLD, 12f));
                    String valStr = String.valueOf(val);
                    int strW = g2.getFontMetrics().stringWidth(valStr);
                    g2.drawString(valStr, x + (barW - strW) / 2, y - 8);
                }

                g2.setColor(key.equals("FF") ? failColor : TEXT_LIGHT);
                g2.setFont(APP_FONT.deriveFont(Font.BOLD, 12f));
                int labelW = g2.getFontMetrics().stringWidth(key);
                g2.drawString(key, x + (barW - labelW) / 2, baselineY + 22);
            }

            g2.dispose();
        }
    }

    private JPanel createTablePanel(String type) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(DARK_BG);

        if (type.equals("MY_COURSES")) {
            JLabel termLabel = new JLabel("Current Term: [" + data.systemConfig.getCurrentYear() + " " + data.systemConfig.getCurrentSemester() + "]");
            termLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
            termLabel.setForeground(ACCENT);
            topHeader.add(termLabel, BorderLayout.WEST);
        }

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (type.equals("TRANSCRIPT") && column == 8) return true;
                return false;
            }
        };

        String[] headers;
        if (type.equals("USER")) {
            headers = new String[]{"ID", "Username", "Role", "Name", "Dept", "Year"};
            model.setColumnIdentifiers(headers);
            for (User u : data.users) {
                String d = "-", y = "-";
                if (u.getRole().equals("STUDENT")) {
                    StudentProfile sp = findStudentProfile(u.getUsername());
                    if (sp != null) { d = sp.getDepartment(); y = sp.getYear(); }
                }
                model.addRow(new Object[]{u.getReferenceId(), u.getUsername(), u.getRole(), u.getFullName(), d, y});
            }
        } else if (type.equals("STUDENT")) {
            headers = new String[]{"No", "Name", "Department", "Year", "User"};
            model.setColumnIdentifiers(headers);
            for (User u : data.users) {
                if (u.getRole().equals("STUDENT")) {
                    StudentProfile profile = findStudentProfile(u.getUsername());
                    if (profile != null) {
                        model.addRow(new Object[]{profile.getStudentId(), profile.getFullName(), profile.getDepartment(), profile.getYear(), profile.getUsername()});
                    }
                }
            }
        } else if (type.equals("COURSE") || type.equals("AVAILABLE_COURSES")) {
            headers = new String[]{"Code", "Course", "Credit", "Quota", "Semester", "Dept", "Instructor"};
            model.setColumnIdentifiers(headers);
            for (Course c : data.courses) {
                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getQuota(), c.getYear(), c.getDepartment(), c.getInstructorUsername()});
            }
        } else if (type.equals("INSTRUCTOR_COURSES")) {
            headers = new String[]{"Code", "Course", "Credit", "Quota", "Semester", "Enrolled"};
            model.setColumnIdentifiers(headers);
            for (Course c : data.courses) {
                if (c.getInstructorUsername().equals(currentUser.getUsername())) {
                    model.addRow(new Object[]{
                        c.getCourseCode(),
                        c.getCourseName(),
                        c.getCredit(),
                        c.getQuota(),
                        c.getYear(),
                        countEnrollmentsForCourse(c.getCourseCode())
                    });
                }
            }
        } else if (type.equals("MY_COURSES")) {
            headers = new String[]{"Code", "Course", "Credit", "Instructor", "Semester", "Class Size"};
            model.setColumnIdentifiers(headers);
            for (Enrollment enrollment : data.enrollments) {
                if (enrollment.getStudentUsername().equals(currentUser.getUsername())
                    && enrollment.getAcademicYear().equals(data.systemConfig.getCurrentYear())
                    && enrollment.getSemester().equals(data.systemConfig.getCurrentSemester())) {
                    Course course = findCourse(enrollment.getCourseCode());
                    if (course != null) {
                        model.addRow(new Object[]{
                            course.getCourseCode(),
                            course.getCourseName(),
                            course.getCredit(),
                            course.getInstructorUsername(),
                            course.getYear(),
                            countEnrollmentsForCourse(enrollment.getCourseCode()) + " / " + course.getQuota()
                        });
                    }
                }
            }
        } else if (type.equals("TRANSCRIPT")) {
            headers = new String[]{"Code", "Course", "Credit", "Midterm", "Final", "Average", "Letter", "Points", "Details"};
            model.setColumnIdentifiers(headers);
            for (GradeRecord grade : data.grades) {
                if (grade.getStudentUsername().equals(currentUser.getUsername())) {
                    Course course = findCourse(grade.getCourseCode());
                    if (course != null) {
                        double average = calculateAverage(grade.getMidterm(), grade.getFinalExam());
                        String letterGrade = calculateLetterGrade(average);
                        model.addRow(new Object[]{
                            course.getCourseCode(),
                            course.getCourseName(),
                            course.getCredit(),
                            grade.getMidterm(),
                            grade.getFinalExam(),
                            String.format("%.2f", average),
                            letterGrade,
                            String.format("%.2f", gradePoint(letterGrade)),
                            "View Stats"
                        });
                    }
                }
            }
        } else {
            headers = new String[]{"Column 1", "Column 2", "Column 3"};
            model.setColumnIdentifiers(headers);
            model.addRow(new Object[]{"Demo", "Data", "-"});
        }

        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        styleTable(table);

        if (currentUser.getRole().equals("ADMIN") && (type.equals("USER") || type.equals("STUDENT") || type.equals("COURSE"))) {
            JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            filterBar.setBackground(PANEL_BG);
            
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"All Roles", "ADMIN", "INSTRUCTOR", "STUDENT"});
            JComboBox<String> deptCombo = new JComboBox<>(new String[]{"All Depts", "CE", "EE", "IE", "ME", "BA"});
            JComboBox<String> yearCombo = new JComboBox<>(new String[]{"All Years", "1", "2", "3", "4"});
            JTextField searchField = new JTextField(12);
            styleTextField(searchField);

            JLabel lRole = new JLabel("Role:"); lRole.setForeground(TEXT_LIGHT);
            JLabel lDept = new JLabel("Dept:"); lDept.setForeground(TEXT_LIGHT);
            JLabel lYear = new JLabel("Year:"); lYear.setForeground(TEXT_LIGHT);
            JLabel lSearch = new JLabel("Search:"); lSearch.setForeground(TEXT_LIGHT);

            if (type.equals("USER")) {
                filterBar.add(lRole); filterBar.add(roleCombo);
                filterBar.add(lDept); filterBar.add(deptCombo);
                filterBar.add(lYear); filterBar.add(yearCombo);
                deptCombo.setVisible(false); lDept.setVisible(false);
                yearCombo.setVisible(false); lYear.setVisible(false);
                roleCombo.addActionListener(e -> {
                    boolean isStudent = roleCombo.getSelectedItem().equals("STUDENT");
                    deptCombo.setVisible(isStudent); lDept.setVisible(isStudent);
                    yearCombo.setVisible(isStudent); lYear.setVisible(isStudent);
                    filterBar.revalidate(); filterBar.repaint();
                });
            } else if (type.equals("STUDENT")) {
                filterBar.add(lDept); filterBar.add(deptCombo);
                filterBar.add(lYear); filterBar.add(yearCombo);
            } else if (type.equals("COURSE")) {
                filterBar.add(lDept); filterBar.add(deptCombo);
                filterBar.add(lYear); filterBar.add(yearCombo);
            }

            filterBar.add(lSearch);
            filterBar.add(searchField);

            java.util.function.Consumer<Object> applier = (e) -> {
                List<RowFilter<Object, Object>> filters = new ArrayList<>();
                
                if (type.equals("USER")) {
                    if (roleCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + roleCombo.getSelectedItem() + "$", 2));
                    if (deptCombo.isVisible() && deptCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + deptCombo.getSelectedItem() + "$", 4));
                    if (yearCombo.isVisible() && yearCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + yearCombo.getSelectedItem() + "$", 5));
                    if (!searchField.getText().isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + searchField.getText(), 3));
                } else if (type.equals("STUDENT")) {
                    if (deptCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + deptCombo.getSelectedItem() + "$", 2));
                    if (yearCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + yearCombo.getSelectedItem() + "$", 3));
                    if (!searchField.getText().isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + searchField.getText(), 1));
                } else if (type.equals("COURSE")) {
                    if (deptCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + deptCombo.getSelectedItem() + "$", 5));
                    if (yearCombo.getSelectedIndex() > 0) filters.add(RowFilter.regexFilter("^" + yearCombo.getSelectedItem() + "$", 4));
                    if (!searchField.getText().isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + searchField.getText(), 0, 1));
                }
                sorter.setRowFilter(RowFilter.andFilter(filters));
            };

            roleCombo.addActionListener(e -> applier.accept(null));
            deptCombo.addActionListener(e -> applier.accept(null));
            yearCombo.addActionListener(e -> applier.accept(null));
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { applier.accept(null); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { applier.accept(null); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { applier.accept(null); }
            });

            panel.add(filterBar, BorderLayout.NORTH);
        } else {
            panel.add(topHeader, BorderLayout.NORTH);
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        if (currentUser.getRole().equals("ADMIN")) {
            // ... (keeping existing admin logic)
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(DARK_BG);
            JButton addButton = new JButton("Add New");
            JButton editButton = new JButton("Edit");
            JButton deleteButton = new JButton("Delete");
            styleButton(addButton);
            styleButton(editButton);
            styleButton(deleteButton);
            deleteButton.setBackground(new Color(225, 29, 72));
            
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);

            table.getSelectionModel().addListSelectionListener(e -> {
                boolean selected = table.getSelectedRow() != -1;
                editButton.setEnabled(selected);
                deleteButton.setEnabled(selected);
            });

            addButton.addActionListener(e -> showAddWindow(type, model));
            editButton.addActionListener(e -> {
                int viewRow = table.getSelectedRow();
                if (viewRow != -1) showEditWindow(type, table.convertRowIndexToModel(viewRow), model);
            });
            deleteButton.addActionListener(e -> {
                int viewRow = table.getSelectedRow();
                if (viewRow != -1) deleteAction(type, table.convertRowIndexToModel(viewRow), model);
            });

            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        } else if (type.equals("TRANSCRIPT")) {
            // Add Button Logic for Details column
            table.getColumnModel().getColumn(8).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JButton b = new JButton(value.toString());
                    styleButton(b);
                    b.setBackground(ACCENT);
                    b.setFont(APP_FONT.deriveFont(Font.BOLD, 10f));
                    return b;
                }
            });

            table.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
                private JButton button;
                private String code;
                {
                    button = new JButton();
                    button.addActionListener(e -> {
                        stopCellEditing();
                        SwingUtilities.invokeLater(() -> showCourseDetailsPopup(code));
                    });
                }
                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                    code = (String) table.getValueAt(row, 0);
                    button.setText(value.toString());
                    styleButton(button);
                    return button;
                }
                @Override
                public Object getCellEditorValue() { return "View Stats"; }
            });

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(DARK_BG);
            
            JLabel gpaLabel = new JLabel("  Weighted GPA: " + String.format("%.2f", calculateWeightedGpa(currentUser.getUsername())));
            gpaLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 16f));
            gpaLabel.setForeground(TEXT_LIGHT);
            
            JButton downloadBtn = new JButton("Print / Save as PDF");
            styleButton(downloadBtn);
            downloadBtn.addActionListener(e -> {
                try {
                    table.print(JTable.PrintMode.FIT_WIDTH, new java.text.MessageFormat("Transcript - " + currentUser.getFullName()), new java.text.MessageFormat("Page {0}"));
                } catch (Exception ex) {
                    Validator.showError("Printing failed: " + ex.getMessage());
                }
            });
            
            bottomPanel.add(gpaLabel, BorderLayout.WEST);
            bottomPanel.add(downloadBtn, BorderLayout.EAST);
            panel.add(bottomPanel, BorderLayout.SOUTH);
        } else if (type.equals("INSTRUCTOR_COURSES")) {
            // ENHANCEMENT: View Students Button for Instructors
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(DARK_BG);
            JButton viewStudentsBtn = new JButton("View Enrolled Students");
            styleButton(viewStudentsBtn);
            viewStudentsBtn.setEnabled(false);

            table.getSelectionModel().addListSelectionListener(e -> {
                viewStudentsBtn.setEnabled(table.getSelectedRow() != -1);
            });

            viewStudentsBtn.addActionListener(e -> {
                int viewRow = table.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    String courseCode = (String) model.getValueAt(modelRow, 0);
                    showEnrolledStudentsPopup(courseCode);
                }
            });

            buttonPanel.add(viewStudentsBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }

        return panel;
    }

    // ENHANCEMENT: Instructor feature to see student list
    private void showCourseDetailsPopup(String courseCode) {
        JDialog dialog = new JDialog(this, "Course Statistics - " + courseCode, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(DARK_BG);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Course course = findCourse(courseCode);
        JLabel titleLabel = new JLabel(course != null ? courseCode + " - " + course.getCourseName() : courseCode);
        titleLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(TEXT_LIGHT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(titleLabel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(DARK_BG);
        GradeChartPanel chartPanel = new GradeChartPanel();

        updateInstructorStats(cardsPanel, chartPanel, courseCode, null, null);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(DARK_BG);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(chartPanel, BorderLayout.CENTER);
        
        content.add(centerPanel, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        styleButton(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(DARK_BG);
        bottom.add(closeBtn);
        content.add(bottom, BorderLayout.SOUTH);

        dialog.add(content);
        dialog.setVisible(true);
    }

    private void showEnrolledStudentsPopup(String courseCode) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Department", "Year"});
        
        for (Enrollment e : data.enrollments) {
            if (e.getCourseCode().equals(courseCode)) {
                StudentProfile sp = findStudentProfile(e.getStudentUsername());
                if (sp != null) {
                    model.addRow(new Object[]{sp.getStudentId(), sp.getFullName(), sp.getDepartment(), sp.getYear()});
                }
            }
        }

        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, scroll, "Students in " + courseCode, JOptionPane.PLAIN_MESSAGE);
    }

    // ENHANCEMENT: Export Transcript to HTML
    private void exportTranscriptToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transcript as HTML (Print to PDF)");
        fileChooser.setSelectedFile(new java.io.File(currentUser.getUsername() + "_transcript.html"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(fileChooser.getSelectedFile()));
                pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Transcript - " + currentUser.getUsername() + "</title><style>");
                pw.println("@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');");
                pw.println("body { font-family: 'Inter', sans-serif; margin: 0; padding: 40px; color: #1f2937; background: #fff; }");
                pw.println(".header { display: flex; justify-content: space-between; align-items: flex-end; border-bottom: 3px solid #2563eb; padding-bottom: 20px; margin-bottom: 40px; }");
                pw.println(".university-info h1 { margin: 0; color: #1e3a8a; font-size: 28px; letter-spacing: 1px; text-transform: uppercase; }");
                pw.println(".university-info p { margin: 5px 0 0; color: #6b7280; font-size: 14px; }");
                pw.println(".student-info { text-align: right; }");
                pw.println(".student-info h2 { margin: 0; font-size: 20px; color: #111827; }");
                pw.println(".student-info p { margin: 5px 0 0; color: #4b5563; font-size: 14px; }");
                pw.println(".gpa-badge { display: inline-block; background: #eff6ff; color: #1d4ed8; padding: 8px 16px; border-radius: 20px; font-weight: 600; font-size: 16px; border: 1px solid #bfdbfe; margin-top: 10px; }");
                pw.println("table { width: 100%; border-collapse: collapse; margin-bottom: 40px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }");
                pw.println("th, td { border: 1px solid #e5e7eb; padding: 12px 16px; text-align: center; font-size: 14px; }");
                pw.println("th { background-color: #f8fafc; color: #334155; font-weight: 600; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; }");
                pw.println("td:nth-child(2) { text-align: left; font-weight: 500; }");
                pw.println("h3 { color: #0f172a; border-left: 4px solid #3b82f6; padding-left: 10px; margin-bottom: 15px; font-size: 18px; }");
                pw.println(".footer { margin-top: 50px; text-align: center; color: #9ca3af; font-size: 12px; border-top: 1px solid #e5e7eb; padding-top: 20px; }");
                pw.println("@media print { body { padding: 0; } @page { margin: 2cm; } .no-print { display: none; } box-shadow: none; }");
                pw.println("</style><script>window.onload = function() { setTimeout(function() { window.print(); }, 500); }</script></head><body>");
                
                StudentProfile sp = findStudentProfile(currentUser.getUsername());
                pw.println("<div class='header'><div class='university-info'><h1>University of Technology</h1><p>Official Academic Transcript</p></div>");
                pw.println("<div class='student-info'>");
                if (sp != null) pw.println("<h2>" + sp.getFullName() + "</h2><p>Student ID: " + sp.getStudentId() + " | Dept: " + sp.getDepartment() + "</p>");
                pw.println("<div class='gpa-badge'>Cumulative GPA: " + String.format("%.2f", data.calculateGPA(currentUser.getUsername())) + "</div></div></div>");
                
                java.util.Map<String, List<Object>> grouped = new java.util.LinkedHashMap<>();
                for (GradeRecord g : data.grades) {
                    if (g.getStudentUsername().equals(currentUser.getUsername())) {
                        String key = g.getAcademicYear() + " " + g.getSemester();
                        grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(g);
                    }
                }
                for (Enrollment e : data.enrollments) {
                    if (e.getStudentUsername().equals(currentUser.getUsername())) {
                        boolean hasGrade = false;
                        for (GradeRecord g : data.grades) {
                            if (g.getStudentUsername().equals(currentUser.getUsername()) && g.getCourseCode().equals(e.getCourseCode())) {
                                hasGrade = true; break;
                            }
                        }
                        if (!hasGrade) {
                            String key = e.getAcademicYear() + " " + e.getSemester();
                            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
                        }
                    }
                }

                for (String key : grouped.keySet()) {
                    pw.println("<h3>" + key + "</h3>");
                    pw.println("<table><tr><th width='15%'>Code</th><th width='40%'>Course Name</th><th width='15%'>Credit</th><th width='15%'>Grade</th><th width='15%'>Points</th></tr>");
                    double termPoints = 0;
                    int termCredits = 0;
                    for (Object obj : grouped.get(key)) {
                        if (obj instanceof GradeRecord) {
                            GradeRecord g = (GradeRecord) obj;
                            Course c = findCourse(g.getCourseCode());
                            if (c != null) {
                                int cred = Integer.parseInt(c.getCredit());
                                double pts = cred * gradePoint(g.getLetterGrade());
                                termPoints += pts;
                                termCredits += cred;
                                pw.println("<tr><td>" + c.getCourseCode() + "</td><td>" + c.getCourseName() + "</td><td>" + cred + "</td><td>" + g.getLetterGrade() + "</td><td>" + String.format("%.1f", pts) + "</td></tr>");
                            }
                        } else if (obj instanceof Enrollment) {
                            Enrollment e = (Enrollment) obj;
                            Course c = findCourse(e.getCourseCode());
                            if (c != null) {
                                pw.println("<tr><td>" + c.getCourseCode() + "</td><td style='color:#6b7280;'>" + c.getCourseName() + " <i>(In Progress)</i></td><td>" + c.getCredit() + "</td><td>-</td><td>-</td></tr>");
                            }
                        }
                    }
                    double termGpa = termCredits > 0 ? termPoints / termCredits : 0;
                    pw.println("<tr><td colspan='4' style='text-align:right; font-weight: 600; background-color: #f8fafc;'>Term GPA:</td><td style='font-weight: 700; background-color: #f8fafc; color: #1d4ed8;'>" + String.format("%.2f", termGpa) + "</td></tr>");
                    pw.println("</table>");
                }
                
                pw.println("<div class='footer'>This document was electronically generated on " + new java.util.Date() + " and is an official record.</div>");
                pw.println("</body></html>");
                pw.close();
                JOptionPane.showMessageDialog(this, "High-Quality Transcript generated!\nThe browser will open and prompt you to save as PDF automatically.", "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                
                try {
                    java.awt.Desktop.getDesktop().browse(fileChooser.getSelectedFile().toURI());
                } catch (Exception ignored) {}
                
            } catch (java.io.IOException ex) {
                Validator.showError("Error exporting transcript: " + ex.getMessage());
            }
        }
    }

    private StudentProfile findStudentProfile(String username) {
        for (StudentProfile sp : data.students) {
            if (sp.getUsername().equals(username)) return sp;
        }
        return null;
    }

    private User findUser(String username) {
        for (User user : data.users) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }

    private Course findCourse(String courseCode) {
        for (Course course : data.courses) {
            if (course.getCourseCode().equals(courseCode)) return course;
        }
        return null;
    }

    private int countEnrollmentsForCourse(String courseCode) {
        int count = 0;
        for (Enrollment enrollment : data.enrollments) {
            if (enrollment.getCourseCode().equals(courseCode)
                && enrollment.getAcademicYear().equals(data.systemConfig.getCurrentYear())
                && enrollment.getSemester().equals(data.systemConfig.getCurrentSemester())) {
                count++;
            }
        }
        return count;
    }

    private boolean isStudentEnrolled(String studentUsername, String courseCode) {
        for (Enrollment enrollment : data.enrollments) {
            if (enrollment.getStudentUsername().equals(studentUsername) 
                && enrollment.getCourseCode().equals(courseCode)
                && enrollment.getAcademicYear().equals(data.systemConfig.getCurrentYear())
                && enrollment.getSemester().equals(data.systemConfig.getCurrentSemester())) {
                return true;
            }
        }
        return false;
    }

    private String formatCourseOption(Course course) {
        return course.getCourseCode() + " - " + course.getCourseName();
    }

    private String getCourseCodeFromOption(String option) {
        int separatorIndex = option.indexOf(" - ");
        if (separatorIndex == -1) return option;
        return option.substring(0, separatorIndex);
    }

    private double calculateAverage(String midterm, String finalExam) {
        try {
            double midtermValue = Double.parseDouble(midterm);
            double finalValue = Double.parseDouble(finalExam);
            return (midtermValue * 0.40) + (finalValue * 0.60);
        } catch (NumberFormatException e) {
            System.err.println("Invalid grade value while calculating average.");
            return 0;
        }
    }

    private String calculateLetterGrade(double average) {
        if (average >= 90) return "AA";
        if (average >= 85) return "BA";
        if (average >= 80) return "BB";
        if (average >= 70) return "CB";
        if (average >= 60) return "CC";
        if (average >= 50) return "DC";
        if (average >= 40) return "DD";
        return "FF";
    }

    private double gradePoint(String letterGrade) {
        if (letterGrade.equals("AA")) return 4.0;
        if (letterGrade.equals("BA")) return 3.5;
        if (letterGrade.equals("BB")) return 3.0;
        if (letterGrade.equals("CB")) return 2.5;
        if (letterGrade.equals("CC")) return 2.0;
        if (letterGrade.equals("DC")) return 1.5;
        if (letterGrade.equals("DD")) return 1.0;
        return 0.0;
    }

    private Integer parseIntegerSilently(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid " + fieldName + " value: " + value);
            return null;
        }
    }

    private double calculateWeightedGpa(String studentUsername) {
        return data.calculateGPA(studentUsername);
    }

    private boolean isUsernameUniqueExcept(String username, String currentUsername) {
        for (User user : data.users) {
            if (!user.getUsername().equals(currentUsername) && user.getUsername().equalsIgnoreCase(username.trim())) {
                Validator.showError("This username already exists.");
                return false;
            }
        }
        return true;
    }

    private boolean isCourseCodeUniqueExcept(String courseCode, String currentCourseCode) {
        for (Course course : data.courses) {
            if (!course.getCourseCode().equals(currentCourseCode) && course.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                Validator.showError("This course code already exists.");
                return false;
            }
        }
        return true;
    }

    private boolean isInstructorUsername(String username) {
        User user = findUser(username.trim());
        if (user == null || !user.getRole().equals("INSTRUCTOR")) {
            Validator.showError("Instructor must be an existing instructor username.");
            return false;
        }
        return true;
    }

    private void updateUsernameReferences(String oldUsername, String newUsername) {
        StudentProfile profile = findStudentProfile(oldUsername);
        if (profile != null) profile.setUsername(newUsername);

        for (Enrollment enrollment : data.enrollments) {
            if (enrollment.getStudentUsername().equals(oldUsername)) {
                enrollment.setStudentUsername(newUsername);
            }
        }

        for (GradeRecord grade : data.grades) {
            if (grade.getStudentUsername().equals(oldUsername)) {
                grade.setStudentUsername(newUsername);
            }
        }

        for (Course course : data.courses) {
            if (course.getInstructorUsername().equals(oldUsername)) {
                course.setInstructorUsername(newUsername);
            }
        }
    }

    private void updateCourseCodeReferences(String oldCourseCode, String newCourseCode) {
        for (Enrollment enrollment : data.enrollments) {
            if (enrollment.getCourseCode().equals(oldCourseCode)) {
                enrollment.setCourseCode(newCourseCode);
            }
        }

        for (GradeRecord grade : data.grades) {
            if (grade.getCourseCode().equals(oldCourseCode)) {
                grade.setCourseCode(newCourseCode);
            }
        }
    }

    private void deleteStudentRelatedData(String username) {
        data.enrollments.removeIf(enrollment -> enrollment.getStudentUsername().equals(username));
        data.grades.removeIf(grade -> grade.getStudentUsername().equals(username));
    }

    private void deleteCourseRelatedData(String courseCode) {
        data.enrollments.removeIf(enrollment -> enrollment.getCourseCode().equals(courseCode));
        data.grades.removeIf(grade -> grade.getCourseCode().equals(courseCode));
    }

    private GradeRecord findGradeRecord(String studentUsername, String courseCode) {
        GradeRecord latest = null;
        for (GradeRecord grade : data.grades) {
            if (grade.getStudentUsername().equals(studentUsername) && grade.getCourseCode().equals(courseCode)) {
                latest = grade;
            }
        }
        return latest;
    }

    private boolean isCourseOwnedByCurrentInstructor(String courseCode) {
        Course course = findCourse(courseCode);
        return course != null && course.getInstructorUsername().equals(currentUser.getUsername());
    }

    private JPanel createGradeEntryPanel() {
        if (!currentUser.getRole().equals("INSTRUCTOR")) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Unauthorized", SwingConstants.CENTER), BorderLayout.CENTER);
            applyTheme(panel);
            return panel;
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);

        // Top: Course Selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(DARK_BG);
        JLabel selectLabel = new JLabel("Select Course to Grade: ");
        selectLabel.setForeground(TEXT_LIGHT);
        JComboBox<String> courseCombo = new JComboBox<>();
        for (Course c : data.courses) {
            if (c.getInstructorUsername().equals(currentUser.getUsername())) {
                courseCombo.addItem(c.getCourseCode() + " - " + c.getCourseName());
            }
        }
        topPanel.add(selectLabel);
        topPanel.add(courseCombo);
        panel.add(topPanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Midterm and Final editable
            }
        };

        model.setColumnIdentifiers(new String[]{"Student Username", "Student Name", "Course Code", "Course", "Midterm", "Final", "Average", "Letter"});

        courseCombo.addActionListener(e -> {
            String selected = (String) courseCombo.getSelectedItem();
            if (selected != null) {
                fillGradeEntryModel(model, getCourseCodeFromOption(selected));
            }
        });

        // Auto-calculate Average and Letter Grade
        model.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 4 || col == 5) {
                    Object m = model.getValueAt(row, 4);
                    Object f = model.getValueAt(row, 5);
                    if (m != null && f != null && !m.toString().isEmpty() && !f.toString().isEmpty()) {
                        try {
                            double avg = calculateAverage(m.toString(), f.toString());
                            // Temporarily remove listener to avoid infinite loop
                            SwingUtilities.invokeLater(() -> {
                                model.setValueAt(String.format("%.2f", avg), row, 6);
                                model.setValueAt(calculateLetterGrade(avg), row, 7);
                            });
                        } catch (Exception ex) {}
                    }
                }
            }
        });

        if (courseCombo.getItemCount() > 0) {
            fillGradeEntryModel(model, getCourseCodeFromOption(courseCombo.getItemAt(0).toString()));
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Grades");
        styleButton(saveButton);
        saveButton.addActionListener(e -> saveGradesFromTable(model));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(DARK_BG);
        buttonPanel.add(saveButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void fillGradeEntryModel(DefaultTableModel model, String courseCode) {
        model.setRowCount(0);
        for (Enrollment enrollment : data.enrollments) {
            if (enrollment.getCourseCode().equals(courseCode)) {
                StudentProfile student = findStudentProfile(enrollment.getStudentUsername());
                Course course = findCourse(enrollment.getCourseCode());
                GradeRecord grade = findGradeRecord(enrollment.getStudentUsername(), enrollment.getCourseCode());

                if (student != null && course != null) {
                    String midterm = grade == null ? "" : grade.getMidterm();
                    String finalExam = grade == null ? "" : grade.getFinalExam();
                    String avgStr = "";
                    String letter = "";
                    if (!midterm.isEmpty() && !finalExam.isEmpty()) {
                        double avg = calculateAverage(midterm, finalExam);
                        avgStr = String.format("%.2f", avg);
                        letter = calculateLetterGrade(avg);
                    }

                    model.addRow(new Object[]{
                        student.getUsername(),
                        student.getFullName(),
                        course.getCourseCode(),
                        course.getCourseName(),
                        midterm,
                        finalExam,
                        avgStr,
                        letter
                    });
                }
            }
        }
    }

    private void saveGradesFromTable(DefaultTableModel model) {
        for (int row = 0; row < model.getRowCount(); row++) {
            String midterm = model.getValueAt(row, 4).toString().trim();
            String finalExam = model.getValueAt(row, 5).toString().trim();

            if (!Validator.validateGrade(midterm, "Midterm") || !Validator.validateGrade(finalExam, "Final")) {
                return;
            }
        }

        for (int row = 0; row < model.getRowCount(); row++) {
            String studentUsername = model.getValueAt(row, 0).toString();
            String courseCode = model.getValueAt(row, 2).toString();
            String midterm = model.getValueAt(row, 4).toString().trim();
            String finalExam = model.getValueAt(row, 5).toString().trim();

            GradeRecord grade = findGradeRecord(studentUsername, courseCode);
            if (grade == null) {
                data.grades.add(new GradeRecord(studentUsername, courseCode, midterm, finalExam, data.systemConfig.getCurrentYear(), data.systemConfig.getCurrentSemester()));
            } else {
                grade.setMidterm(midterm);
                grade.setFinalExam(finalExam);
                // Also update term context if it's being graded in a new term
                grade.setAcademicYear(data.systemConfig.getCurrentYear());
                grade.setSemester(data.systemConfig.getCurrentSemester());
            }
        }

        data.saveData();
        JOptionPane.showMessageDialog(this, "Grades saved successfully!");
        refreshDashboard();
    }

    private JPanel createEnrollmentPanel() {
        if (!currentUser.getRole().equals("STUDENT")) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Unauthorized", SwingConstants.CENTER), BorderLayout.CENTER);
            applyTheme(panel);
            return panel;
        }

        StudentProfile sp = findStudentProfile(currentUser.getUsername());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(DARK_BG);

        JLabel currentTermLabel = new JLabel("Current Term: [" + data.systemConfig.getCurrentYear() + " " + data.systemConfig.getCurrentSemester() + "]");
        currentTermLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
        currentTermLabel.setForeground(ACCENT);
        currentTermLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 0));
        topContainer.add(currentTermLabel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(DARK_BG);
        
        JLabel yearFilterLabel = new JLabel("Year:");
        yearFilterLabel.setForeground(TEXT_LIGHT);
        JComboBox<String> yearFilterCombo = new JComboBox<>(new String[]{"All", "1", "2", "3", "4"});
        yearFilterCombo.setSelectedItem(sp != null ? sp.getYear() : "1");

        JLabel termFilterLabel = new JLabel("Term:");
        termFilterLabel.setForeground(TEXT_LIGHT);
        JComboBox<String> termFilterCombo = new JComboBox<>(new String[]{"All", "Fall", "Spring"});
        termFilterCombo.setSelectedItem(data.systemConfig.getCurrentSemester());

        JLabel statsLabel = new JLabel();
        statsLabel.setForeground(TEXT_LIGHT);

        filterPanel.add(yearFilterLabel);
        filterPanel.add(yearFilterCombo);
        filterPanel.add(termFilterLabel);
        filterPanel.add(termFilterCombo);
        filterPanel.add(statsLabel);

        topContainer.add(filterPanel, BorderLayout.CENTER);
        panel.add(topContainer, BorderLayout.NORTH);

        DefaultTableModel availableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 8; 
            }
        };
        availableModel.setColumnIdentifiers(new String[]{"Code", "Course", "Credit", "Quota", "Semester", "Dept", "Enrolled", "Instructor", "Action"});

        JTable availableTable = new JTable(availableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(availableModel);
        availableTable.setRowSorter(sorter);
        styleTable(availableTable);

        boolean isCurrentFall = data.systemConfig.getCurrentSemester().equalsIgnoreCase("Fall");

        // Action Renderer for Available Table
        availableTable.getColumnModel().getColumn(8).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String valStr = value == null ? "" : value.toString();
                JButton b = new JButton(valStr);
                styleSmallButton(b);
                String courseSemStr = (String) table.getValueAt(row, 4);
                boolean isCourseFall = true;
                try {
                    isCourseFall = Integer.parseInt(courseSemStr) % 2 != 0;
                } catch (Exception ignored) {}

                if (valStr.equals("Enrolled") || valStr.equals("In Draft") || (isCourseFall != isCurrentFall)) {
                    b.setBackground(Color.GRAY);
                    b.setEnabled(false);
                    if (isCourseFall != isCurrentFall && !valStr.equals("Enrolled") && !valStr.equals("In Draft")) {
                        b.setText("Not Offered");
                    }
                } else if (valStr.startsWith("Upgrade")) {
                    b.setBackground(new Color(245, 158, 11)); // Orange
                    b.setEnabled(true);
                } else if (valStr.startsWith("Retake")) {
                    b.setBackground(new Color(239, 68, 68)); // Red
                    b.setEnabled(true);
                } else {
                    b.setBackground(ACCENT);
                    b.setEnabled(true);
                }
                b.setForeground(Color.WHITE);
                b.setFont(APP_FONT.deriveFont(Font.BOLD, 11f));
                
                JPanel p = new JPanel(new GridBagLayout());
                p.setOpaque(true);
                p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                p.add(b);
                return p;
            }
        });

        // Action Editor for Available Table
        DefaultTableModel finalDraftModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        
        Runnable refreshTables = () -> {
            fillAvailableCoursesModel(availableModel);
            fillDraftCoursesModel(finalDraftModel);
            updateEnrollmentStats(statsLabel);
        };

        availableTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private String label;
            private JButton button;
            private JPanel panel;
            private String courseCode;
            private boolean isCourseFall;

            {
                button = new JButton();
                styleSmallButton(button);
                button.addActionListener(e -> {
                    if (!label.equals("Enrolled") && !label.equals("In Draft") && (isCourseFall == isCurrentFall)) {
                        stopCellEditing();
                        SwingUtilities.invokeLater(() -> {
                            addToDraft(courseCode);
                            refreshTables.run();
                        });
                    } else {
                        stopCellEditing();
                    }
                });
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(true);
                panel.add(button);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                label = (value == null) ? "" : value.toString();
                courseCode = (String) table.getValueAt(row, 0);
                String courseSemStr = (String) table.getValueAt(row, 4);
                isCourseFall = true;
                try {
                    isCourseFall = Integer.parseInt(courseSemStr) % 2 != 0;
                } catch (Exception ignored) {}

                button.setText(label);
                
                if (label.equals("Enrolled") || label.equals("In Draft") || (isCourseFall != isCurrentFall)) {
                    button.setBackground(Color.GRAY);
                    button.setEnabled(false);
                    if (isCourseFall != isCurrentFall && !label.equals("Enrolled") && !label.equals("In Draft")) {
                        button.setText("Not Offered");
                    }
                } else if (label.startsWith("Upgrade")) {
                    button.setBackground(new Color(245, 158, 11)); // Orange
                    button.setEnabled(true);
                } else if (label.startsWith("Retake")) {
                    button.setBackground(new Color(239, 68, 68)); // Red
                    button.setEnabled(true);
                } else {
                    button.setBackground(ACCENT);
                    button.setEnabled(true);
                }
                button.setForeground(Color.WHITE);
                
                panel.setBackground(table.getSelectionBackground());
                return panel;
            }

            @Override
            public Object getCellEditorValue() { return label; }
        });

        // Draft Section setup
        finalDraftModel.setColumnIdentifiers(new String[]{"Code", "Course", "Credit", "Semester", "Dept", "Action"});
        JTable draftTable = new JTable(finalDraftModel);
        styleTable(draftTable);

        draftTable.getColumnModel().getColumn(5).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String valStr = value == null ? "" : value.toString();
                JButton b = new JButton(valStr);
                styleSmallButton(b);
                if (valStr.equals("Confirmed")) {
                    b.setBackground(Color.GRAY);
                    b.setEnabled(false);
                } else {
                    b.setBackground(new Color(239, 68, 68));
                    b.setEnabled(true);
                }
                
                JPanel p = new JPanel(new GridBagLayout());
                p.setOpaque(true);
                p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                p.add(b);
                return p;
            }
        });

        draftTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton button;
            private JPanel panel;
            private String courseCode;
            private String label;

            {
                button = new JButton();
                styleSmallButton(button);
                button.addActionListener(e -> {
                    if ("Remove".equals(label)) {
                        stopCellEditing();
                        SwingUtilities.invokeLater(() -> {
                            draftEnrollments.remove(courseCode);
                            refreshTables.run();
                        });
                    } else {
                        stopCellEditing();
                    }
                });
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(true);
                panel.add(button);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                label = value == null ? "" : value.toString();
                courseCode = table.getValueAt(row, 0).toString();
                button.setText(label);
                
                if (label.equals("Confirmed")) {
                    button.setBackground(Color.GRAY);
                    button.setEnabled(false);
                } else {
                    button.setBackground(new Color(239, 68, 68));
                    button.setEnabled(true);
                }
                
                panel.setBackground(table.getSelectionBackground());
                return panel;
            }
            @Override
            public Object getCellEditorValue() { return label; }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(10);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setBackground(DARK_BG);

        JPanel topHalf = new JPanel(new BorderLayout());
        topHalf.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PANEL_BG_LIGHT), "Available Courses", 0, 0, APP_FONT, TEXT_LIGHT));
        topHalf.setBackground(DARK_BG);
        topHalf.add(new JScrollPane(availableTable), BorderLayout.CENTER);

        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PANEL_BG_LIGHT), "Draft (Selected) Courses", 0, 0, APP_FONT, TEXT_LIGHT));
        bottomHalf.setBackground(DARK_BG);
        bottomHalf.add(new JScrollPane(draftTable), BorderLayout.CENTER);

        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        confirmPanel.setBackground(DARK_BG);
        JButton confirmBtn = new JButton("Confirm & Send for Approval");
        styleButton(confirmBtn);
        confirmBtn.setBackground(new Color(34, 197, 94));
        confirmBtn.addActionListener(e -> {
            if (draftEnrollments.isEmpty()) {
                Validator.showError("No draft courses to confirm.");
                return;
            }
            for (String c : draftEnrollments) {
                data.enrollments.add(new Enrollment(currentUser.getUsername(), c, data.systemConfig.getCurrentYear(), data.systemConfig.getCurrentSemester()));
            }
            draftEnrollments.clear();
            data.saveData();
            JOptionPane.showMessageDialog(this, "Enrollment confirmed successfully! Courses are now officially added to your schedule.");
            refreshTables.run();
        });
        confirmPanel.add(confirmBtn);
        bottomHalf.add(confirmPanel, BorderLayout.SOUTH);

        splitPane.setTopComponent(topHalf);
        splitPane.setBottomComponent(bottomHalf);

        panel.add(splitPane, BorderLayout.CENTER);
        
        refreshTables.run();

        Runnable filterAction = () -> {
            String yearText = (String) yearFilterCombo.getSelectedItem();
            String termText = (String) termFilterCombo.getSelectedItem();
            
            List<RowFilter<Object, Object>> filters = new ArrayList<>();
            
            if (!yearText.equals("All")) {
                int y = Integer.parseInt(yearText);
                String sem1 = String.valueOf((y - 1) * 2 + 1);
                String sem2 = String.valueOf((y - 1) * 2 + 2);
                filters.add(RowFilter.regexFilter("^(" + sem1 + "|" + sem2 + ")$", 4));
            }
            
            if (!termText.equals("All")) {
                boolean filterFall = termText.equals("Fall");
                filters.add(new RowFilter<Object, Object>() {
                    @Override
                    public boolean include(Entry<?, ?> entry) {
                        try {
                            int sem = Integer.parseInt(entry.getStringValue(4));
                            return filterFall ? (sem % 2 != 0) : (sem % 2 == 0);
                        } catch (Exception e) { return true; }
                    }
                });
            }
            
            sorter.setRowFilter(RowFilter.andFilter(filters));
        };

        yearFilterCombo.addActionListener(e -> filterAction.run());
        termFilterCombo.addActionListener(e -> filterAction.run());
        
        filterAction.run();

        return panel;
    }

    private void fillDraftCoursesModel(DefaultTableModel model) {
        model.setRowCount(0);
        // Add confirmed courses for the current semester
        for (Enrollment e : data.enrollments) {
            if (e.getStudentUsername().equals(currentUser.getUsername()) &&
                e.getAcademicYear().equals(data.systemConfig.getCurrentYear()) &&
                e.getSemester().equals(data.systemConfig.getCurrentSemester())) {
                Course c = findCourse(e.getCourseCode());
                if (c != null) {
                    model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getYear(), c.getDepartment(), "Confirmed"});
                }
            }
        }
        // Add draft courses
        for (String code : draftEnrollments) {
            Course c = findCourse(code);
            if (c != null) {
                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getYear(), c.getDepartment(), "Remove"});
            }
        }
    }

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topHeader.setBackground(DARK_BG);

        JLabel termLabel = new JLabel("Select Term: ");
        termLabel.setForeground(TEXT_LIGHT);
        termLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
        
        JComboBox<String> termCombo = new JComboBox<>();
        Set<String> terms = new LinkedHashSet<>();
        for (GradeRecord g : data.grades) {
            if (g.getStudentUsername().equals(currentUser.getUsername())) {
                terms.add(g.getAcademicYear() + " " + g.getSemester());
            }
        }
        String currentTermStr = data.systemConfig.getCurrentYear() + " " + data.systemConfig.getCurrentSemester();
        terms.add(currentTermStr);
        
        for (String t : terms) termCombo.addItem(t);
        termCombo.setSelectedItem(currentTermStr);

        topHeader.add(termLabel);
        topHeader.add(termCombo);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{"Code", "Course Name", "Credit", "Midterm", "Final", "Letter Grade"});
        JTable table = new JTable(model);
        styleTable(table);

        Runnable fillTable = () -> {
            model.setRowCount(0);
            String selected = (String) termCombo.getSelectedItem();
            if (selected == null) return;
            String[] parts = selected.split(" ");
            if (parts.length < 2) return;
            String ay = parts[0];
            String sem = parts[1];

            if (selected.equals(currentTermStr)) {
                for (Enrollment e : data.enrollments) {
                    if (e.getStudentUsername().equals(currentUser.getUsername()) &&
                        e.getAcademicYear().equals(ay) && e.getSemester().equals(sem)) {
                        Course c = findCourse(e.getCourseCode());
                        if (c != null) {
                            GradeRecord gr = findGradeRecordExactTerm(currentUser.getUsername(), c.getCourseCode(), ay, sem);
                            if (gr != null) {
                                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), gr.getMidterm(), gr.getFinalExam(), gr.getLetterGrade()});
                            } else {
                                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), "-", "-", "-"});
                            }
                        }
                    }
                }
            } else {
                for (GradeRecord g : data.grades) {
                    if (g.getStudentUsername().equals(currentUser.getUsername()) &&
                        g.getAcademicYear().equals(ay) && g.getSemester().equals(sem)) {
                        Course c = findCourse(g.getCourseCode());
                        if (c != null) {
                            model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), g.getMidterm(), g.getFinalExam(), g.getLetterGrade()});
                        }
                    }
                }
            }
        };

        termCombo.addActionListener(e -> fillTable.run());
        fillTable.run();

        panel.add(topHeader, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private GradeRecord findGradeRecordExactTerm(String username, String code, String ay, String sem) {
        for (GradeRecord g : data.grades) {
            if (g.getStudentUsername().equals(username) && g.getCourseCode().equals(code) &&
                g.getAcademicYear().equals(ay) && g.getSemester().equals(sem)) {
                return g;
            }
        }
        return null;
    }


    private int calculateDraftCredits() {
        int total = 0;
        for (String code : draftEnrollments) {
            Course c = findCourse(code);
            if (c != null) total += Integer.parseInt(c.getCredit());
        }
        return total;
    }

    private void addToDraft(String courseCode) {
        Course selectedCourse = findCourse(courseCode);
        if (selectedCourse == null) return;

        if (draftEnrollments.contains(courseCode)) {
            Validator.showError("Already in draft!");
            return;
        }

        // Rule: Quota check
        Integer quota = Validator.parseInteger(selectedCourse.getQuota(), "Quota");
        if (quota != null && countEnrollmentsForCourse(courseCode) >= quota) {
            Validator.showError("This course is full.");
            return;
        }

        // Rule: Credit limit (36 ECTS)
        int currentCredits = calculateCurrentSemesterCredits();
        int draftCredits = calculateDraftCredits();
        int newCredit = Integer.parseInt(selectedCourse.getCredit());
        if (currentCredits + draftCredits + newCredit > 36) {
            Validator.showError("Credit limit exceeded! Max 36 ECTS allowed per semester. Current + Draft: " + (currentCredits + draftCredits));
            return;
        }

        // Rule: Upper level check (GPA > 3.60)
        StudentProfile sp = findStudentProfile(currentUser.getUsername());
        int studentSemester = getCurrentSemesterNumber(sp != null ? sp.getYear() : "1");
        int courseSemester = Integer.parseInt(selectedCourse.getYear()); // "Year" is now Semester (1-8)
        double gpa = data.calculateGPA(currentUser.getUsername());
        
        if (courseSemester > studentSemester && gpa < 3.60) {
            Validator.showError("Your GPA (" + String.format("%.2f", gpa) + ") is below 3.60. You cannot take courses from higher semesters.");
            return;
        }

        draftEnrollments.add(courseCode);
    }

    private int calculateCurrentSemesterCredits() {
        int total = 0;
        for (Enrollment e : data.enrollments) {
            if (e.getStudentUsername().equals(currentUser.getUsername()) 
                && e.getAcademicYear().equals(data.systemConfig.getCurrentYear())
                && e.getSemester().equals(data.systemConfig.getCurrentSemester())) {
                Course c = findCourse(e.getCourseCode());
                if (c != null) total += Integer.parseInt(c.getCredit());
            }
        }
        return total;
    }

    private void updateEnrollmentStats(JLabel label) {
        int credits = calculateCurrentSemesterCredits();
        int draftCredits = calculateDraftCredits();
        double gpa = data.calculateGPA(currentUser.getUsername());
        label.setText(" |  Enrolled Credits: " + credits + "/36  |  Draft Credits: " + draftCredits + "/36  |  GPA: " + String.format("%.2f", gpa));
    }

    private void fillAvailableCoursesModel(DefaultTableModel model) {
        StudentProfile sp = findStudentProfile(currentUser.getUsername());
        String studentDept = sp != null ? sp.getDepartment() : "CE";
        List<Curriculum> myCurriculum = data.getCurriculumForDept(studentDept);
        List<String> validCourseCodes = new ArrayList<>();
        for (Curriculum c : myCurriculum) validCourseCodes.add(c.getCourseCode());

        model.setRowCount(0);
        for (Course course : data.courses) {
            // Only show courses in the student's curriculum
            if (!validCourseCodes.contains(course.getCourseCode())) continue;

            String actionLabel = "Enroll";
            if (isStudentEnrolled(currentUser.getUsername(), course.getCourseCode())) {
                actionLabel = "Enrolled";
            } else if (draftEnrollments.contains(course.getCourseCode())) {
                actionLabel = "In Draft";
            } else {
                GradeRecord gr = findGradeRecord(currentUser.getUsername(), course.getCourseCode());
                if (gr != null) {
                    String lg = gr.getLetterGrade();
                    if (lg.equals("FF") || lg.equals("FD") || lg.equals("NA") || lg.equals("N/A")) {
                        actionLabel = "Retake (" + lg + ")";
                    } else {
                        actionLabel = "Upgrade (" + lg + ")";
                    }
                }
            }

            model.addRow(new Object[]{
                course.getCourseCode(),
                course.getCourseName(),
                course.getCredit(),
                course.getQuota(),
                course.getYear(),
                course.getDepartment(),
                countEnrollmentsForCourse(course.getCourseCode()),
                course.getInstructorUsername(),
                actionLabel
            });
        }
    }

    private void fillEnrollmentCombo(JComboBox<String> courseCombo) {
        courseCombo.removeAllItems();
        for (Course course : data.courses) {
            Integer quota = Validator.parseInteger(course.getQuota(), "Quota");
            boolean hasCapacity = quota != null && countEnrollmentsForCourse(course.getCourseCode()) < quota;
            if (!isStudentEnrolled(currentUser.getUsername(), course.getCourseCode()) && hasCapacity) {
                courseCombo.addItem(formatCourseOption(course));
            }
        }
    }



    private int getCurrentSemesterNumber(String studentYearStr) {
        try {
            int year = Integer.parseInt(studentYearStr);
            boolean isSpring = data.systemConfig.getCurrentSemester().equalsIgnoreCase("Spring");
            return (year - 1) * 2 + (isSpring ? 2 : 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void showAddWindow(String type, DefaultTableModel model) {
        if (type.equals("COURSE")) {
            JTextField t1 = new JTextField();
            JTextField t2 = new JTextField();
            JComboBox<String> c1 = new JComboBox<>(new String[]{"2", "3", "4", "5", "6"});
            JTextField t3 = new JTextField();
            JComboBox<String> cYear = new JComboBox<>(new String[]{"1", "2", "3", "4"});
            JComboBox<String> cDept = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            JTextField t4 = new JTextField();

            Object[] form = { "Code:", t1, "Name:", t2, "Credit:", c1, "Quota:", t3, "Year:", cYear, "Dept:", cDept, "Instructor:", t4 };
            int res = JOptionPane.showConfirmDialog(this, form, "Add Course", JOptionPane.OK_CANCEL_OPTION);
            
            if (res == JOptionPane.OK_OPTION
                    && Validator.validateRequiredText(t1.getText(), "Course code")
                    && Validator.validateRequiredText(t2.getText(), "Course name")
                    && Validator.validateQuota(t3.getText())
                    && Validator.validateRequiredText(t4.getText(), "Instructor username")
                    && Validator.isCourseCodeUnique(t1.getText(), data.courses)
                    && isInstructorUsername(t4.getText())) {
                Course c = new Course(t1.getText().trim(), t2.getText().trim(), c1.getSelectedItem().toString(), t3.getText().trim(), t4.getText().trim(), cYear.getSelectedItem().toString(), cDept.getSelectedItem().toString());
                data.courses.add(c);
                data.saveData();
                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getQuota(), c.getYear(), c.getDepartment(), c.getInstructorUsername()});
                refreshDashboard();
            }
        } else if (type.equals("USER") || type.equals("STUDENT")) {
            // TASK 1 & 3: Auto-ID Generation and Role Handling
            JTextField uField = new JTextField();
            JTextField pField = new JTextField();
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "INSTRUCTOR", "STUDENT"});
            JTextField nameField = new JTextField();
            
            // For Students, we need extra fields
            JComboBox<String> deptCombo = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            JTextField yearField = new JTextField();

            // Set role fixed if we are in Student tab
            if (type.equals("STUDENT")) {
                roleCombo.setSelectedItem("STUDENT");
                roleCombo.setEnabled(false);
            }

            Object[] form = {
                "Username:", uField,
                "Password:", pField,
                "Role:", roleCombo,
                "Full Name:", nameField,
                "Department (if Student):", deptCombo,
                "Year (if Student):", yearField
            };

            int res = JOptionPane.showConfirmDialog(this, form, "Add Record", JOptionPane.OK_CANCEL_OPTION);

            if (res == JOptionPane.OK_OPTION
                    && Validator.validateRequiredText(uField.getText(), "Username")
                    && Validator.validateRequiredText(pField.getText(), "Password")
                    && Validator.validateRequiredText(nameField.getText(), "Full name")
                    && Validator.isUsernameUnique(uField.getText(), data.users)) {
                String role = (String) roleCombo.getSelectedItem();

                if (role.equals("STUDENT")
                        && !Validator.validateYear(yearField.getText())) {
                    return;
                }

                // TASK 3: Auto-ID Generation logic
                String autoId = data.generateNextId(role);
                
                User u = new User(uField.getText().trim(), pField.getText().trim(), role, nameField.getText().trim(), autoId);
                data.users.add(u);

                if (role.equals("STUDENT")) {
                    StudentProfile sp = new StudentProfile(autoId, nameField.getText().trim(), (String)deptCombo.getSelectedItem(), yearField.getText().trim(), uField.getText().trim());
                    data.students.add(sp);
                }
                
                data.saveData();

                // TASK 5: Update model directly
                if (type.equals("USER")) {
                    model.addRow(new Object[]{u.getReferenceId(), u.getUsername(), u.getRole(), u.getFullName()});
                } else if (type.equals("STUDENT") && role.equals("STUDENT")) {
                    StudentProfile sp = findStudentProfile(u.getUsername());
                    model.addRow(new Object[]{sp.getStudentId(), sp.getFullName(), sp.getDepartment(), sp.getYear(), sp.getUsername()});
                }
                
                JOptionPane.showMessageDialog(this, "Record added successfully! ID: " + autoId);
                refreshDashboard();
            }
        }
    }

    private void showEditWindow(String type, int modelRow, DefaultTableModel model) {
        if (type.equals("COURSE")) {
            Course c = data.courses.get(modelRow);
            String oldCourseCode = c.getCourseCode();
            JTextField t1 = new JTextField(c.getCourseCode());
            JTextField t2 = new JTextField(c.getCourseName());
            JComboBox<String> c1 = new JComboBox<>(new String[]{"2", "3", "4", "5", "6"});
            c1.setSelectedItem(c.getCredit());
            JTextField t3 = new JTextField(c.getQuota());
            JComboBox<String> cYear = new JComboBox<>(new String[]{"1", "2", "3", "4"});
            cYear.setSelectedItem(c.getYear());
            JComboBox<String> cDept = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            cDept.setSelectedItem(c.getDepartment());
            JTextField t4 = new JTextField(c.getInstructorUsername());

            Object[] form = { "Code:", t1, "Name:", t2, "Credit:", c1, "Quota:", t3, "Year:", cYear, "Dept:", cDept, "Instructor:", t4 };
            if (JOptionPane.showConfirmDialog(this, form, "Edit Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (!Validator.validateRequiredText(t1.getText(), "Course code")
                        || !Validator.validateRequiredText(t2.getText(), "Course name")
                        || !Validator.validateQuota(t3.getText())
                        || !Validator.validateRequiredText(t4.getText(), "Instructor username")
                        || !isCourseCodeUniqueExcept(t1.getText(), oldCourseCode)
                        || !isInstructorUsername(t4.getText())) {
                    return;
                }

                String newCourseCode = t1.getText().trim();
                c.setCourseCode(newCourseCode);
                c.setCourseName(t2.getText().trim());
                c.setCredit(c1.getSelectedItem().toString());
                c.setQuota(t3.getText().trim());
                c.setYear(cYear.getSelectedItem().toString());
                c.setDepartment(cDept.getSelectedItem().toString());
                c.setInstructorUsername(t4.getText().trim());
                updateCourseCodeReferences(oldCourseCode, newCourseCode);
                data.saveData();
                model.setValueAt(c.getCourseCode(), modelRow, 0);
                model.setValueAt(c.getCourseName(), modelRow, 1);
                model.setValueAt(c.getCredit(), modelRow, 2);
                model.setValueAt(c.getQuota(), modelRow, 3);
                model.setValueAt(c.getYear(), modelRow, 4);
                model.setValueAt(c.getDepartment(), modelRow, 5);
                model.setValueAt(c.getInstructorUsername(), modelRow, 6);
                refreshDashboard();
            }
        } else if (type.equals("USER")) {
            User u = data.users.get(modelRow);
            String oldUsername = u.getUsername();
            JTextField t1 = new JTextField(u.getUsername());
            JTextField t2 = new JTextField(u.getFullName());
            Object[] form = { "Username:", t1, "Name:", t2 };
            if (JOptionPane.showConfirmDialog(this, form, "Edit User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (!Validator.validateRequiredText(t1.getText(), "Username")
                        || !Validator.validateRequiredText(t2.getText(), "Full name")
                        || !isUsernameUniqueExcept(t1.getText(), oldUsername)) {
                    return;
                }

                String newUsername = t1.getText().trim();
                u.setUsername(newUsername);
                u.setFullName(t2.getText().trim());
                updateUsernameReferences(oldUsername, newUsername);

                StudentProfile profile = findStudentProfile(newUsername);
                if (profile != null) profile.setFullName(u.getFullName());

                data.saveData();
                model.setValueAt(u.getUsername(), modelRow, 1);
                model.setValueAt(u.getFullName(), modelRow, 3);
                refreshDashboard();
            }
        } else if (type.equals("STUDENT")) {
            // Find student profile by the username in the table
            String username = (String) model.getValueAt(modelRow, 4);
            StudentProfile s = findStudentProfile(username);
            if (s == null) return;

            JTextField t1 = new JTextField(s.getFullName());
            JComboBox<String> cDept = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            cDept.setSelectedItem(s.getDepartment());
            JTextField t3 = new JTextField(s.getYear());
            Object[] form = { "Name:", t1, "Dept:", cDept, "Year:", t3 };
            if (JOptionPane.showConfirmDialog(this, form, "Edit Student", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (!Validator.validateRequiredText(t1.getText(), "Full name")
                        || !Validator.validateYear(t3.getText())) {
                    return;
                }

                s.setFullName(t1.getText().trim());
                s.setDepartment((String)cDept.getSelectedItem());
                s.setYear(t3.getText().trim());

                User user = findUser(username);
                if (user != null) user.setFullName(s.getFullName());

                data.saveData();
                model.setValueAt(s.getFullName(), modelRow, 1);
                model.setValueAt(s.getDepartment(), modelRow, 2);
                model.setValueAt(s.getYear(), modelRow, 3);
                refreshDashboard();
            }
        }
    }

    private void deleteAction(String type, int modelRow, DefaultTableModel model) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (type.equals("COURSE")) {
                Course course = data.courses.get(modelRow);
                deleteCourseRelatedData(course.getCourseCode());
                data.courses.remove(modelRow);
            } else if (type.equals("USER")) {
                User u = data.users.get(modelRow);
                if (u.getRole().equals("STUDENT")) {
                    StudentProfile sp = findStudentProfile(u.getUsername());
                    if (sp != null) data.students.remove(sp);
                    deleteStudentRelatedData(u.getUsername());
                } else if (u.getRole().equals("INSTRUCTOR")) {
                    for (Course course : data.courses) {
                        if (course.getInstructorUsername().equals(u.getUsername())) {
                            course.setInstructorUsername("");
                        }
                    }
                }
                data.users.remove(modelRow);
            } else if (type.equals("STUDENT")) {
                String username = (String) model.getValueAt(modelRow, 4);
                StudentProfile sp = findStudentProfile(username);
                if (sp != null) data.students.remove(sp);
                deleteStudentRelatedData(username);
                
                // Also remove the user record
                for (User u : data.users) {
                    if (u.getUsername().equals(username)) {
                        data.users.remove(u);
                        break;
                    }
                }
            }
            
            data.saveData();
            // TASK 5: Update model directly
            model.removeRow(modelRow);
            refreshDashboard();
        }
    }

    private JPanel createReportPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 30));
        main.setBackground(DARK_BG);
        main.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Top: Summary Cards (Compact)
        JPanel topCards = new JPanel(new GridLayout(1, 3, 20, 0));
        topCards.setBackground(DARK_BG);
        topCards.add(createReportCard("TOTAL USERS", String.valueOf(data.users.size()), new Color(79, 70, 229)));
        topCards.add(createReportCard("STUDENTS", String.valueOf(data.students.size()), new Color(16, 185, 129)));
        topCards.add(createReportCard("COURSES", String.valueOf(data.courses.size()), new Color(245, 158, 11)));
        
        // Center: Department Analytics
        JPanel analyticsPanel = new JPanel(new BorderLayout());
        analyticsPanel.setBackground(PANEL_BG);
        analyticsPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(PANEL_BG_LIGHT, 20),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Department Analytics Overview");
        title.setFont(APP_FONT.deriveFont(Font.BOLD, 18f));
        title.setForeground(TEXT_LIGHT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        analyticsPanel.add(title, BorderLayout.NORTH);

        String[] headers = {"Department", "Total Students", "Average GPA"};
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        String[] depts = {"CE", "EE", "IE", "ME", "BA"};
        for (String dept : depts) {
            int count = 0;
            double totalGpa = 0;
            int gpaCount = 0;
            for (StudentProfile sp : data.students) {
                if (sp.getDepartment().equals(dept)) {
                    count++;
                    double gpa = calculateWeightedGpa(sp.getUsername());
                    if (gpa > 0) {
                        totalGpa += gpa;
                        gpaCount++;
                    }
                }
            }
            double avgGpa = gpaCount > 0 ? totalGpa / gpaCount : 0.0;
            model.addRow(new Object[]{dept, count, String.format("%.2f", avgGpa)});
        }

        JTable table = new JTable(model);
        styleTable(table);
        analyticsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        main.add(topCards, BorderLayout.NORTH);
        main.add(analyticsPanel, BorderLayout.CENTER);
        
        return main;
    }

    // ENHANCEMENT: Visual Dashboard Card
    private JPanel createReportCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(accent, 20),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 12f));
        titleLabel.setForeground(TEXT_MUTED);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 36f));
        valueLabel.setForeground(Color.WHITE);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private static Font loadAppFont() {
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String fontName : graphics.getAvailableFontFamilyNames()) {
            if (fontName.equalsIgnoreCase("Inter")) {
                return new Font(fontName, Font.PLAIN, 15);
            }
        }
        return new Font("Segoe UI", Font.PLAIN, 15);
    }    private void styleTextField(JTextField field) {
        field.setFont(APP_FONT.deriveFont(14f));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search...");
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
    }

    private void styleButton(JButton button) {
        button.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
    }

    private void styleSmallButton(JButton button) {
        button.setFont(APP_FONT.deriveFont(Font.BOLD, 11f));
        button.setForeground(Color.WHITE);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 2,10,2,10;");
    }

    private void styleTable(JTable table) {
        table.setFont(APP_FONT.deriveFont(14f));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(71, 85, 105)); // Subtle horizontal lines
        table.putClientProperty(FlatClientProperties.STYLE, "rowHoverBackground: $Table.selectionInactiveBackground");
        
        JTableHeader header = table.getTableHeader();
        header.setFont(APP_FONT.deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void applyTheme(Container container) {
        // Disabled since FlatLaf handles deep component theming perfectly and natively.
    }

    private JPanel createCurriculumView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        StudentProfile sp = findStudentProfile(currentUser.getUsername());
        String dept = sp != null ? sp.getDepartment() : "CE";
        
        JLabel title = new JLabel("Curriculum Grid - " + dept + " Department");
        title.setFont(APP_FONT.deriveFont(Font.BOLD, 22f));
        title.setForeground(TEXT_LIGHT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
        grid.setBackground(DARK_BG);

        List<Curriculum> currList = data.getCurriculumForDept(dept);
        for (int sem = 1; sem <= 8; sem++) {
            JPanel semPanel = new JPanel(new BorderLayout());
            semPanel.setBackground(PANEL_BG);
            semPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(PANEL_BG_LIGHT, 12),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JLabel semLabel = new JLabel("Semester " + sem);
            semLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
            semLabel.setForeground(ACCENT);
            semPanel.add(semLabel, BorderLayout.NORTH);

            JPanel coursesBox = new JPanel();
            coursesBox.setLayout(new BoxLayout(coursesBox, BoxLayout.Y_AXIS));
            coursesBox.setBackground(PANEL_BG);

            int totalECTS = 0;
            for (Curriculum c : currList) {
                if (c.getSemester() == sem) {
                    Course course = findCourse(c.getCourseCode());
                    if (course != null) {
                        int credit = Integer.parseInt(course.getCredit());
                        totalECTS += credit;
                        
                        String status = "Not Taken";
                        Color statusColor = TEXT_MUTED;
                        GradeRecord gr = findGradeRecord(currentUser.getUsername(), course.getCourseCode());
                        if (gr != null) {
                            String lg = gr.getLetterGrade();
                            if (lg.equals("FF") || lg.equals("FD") || lg.equals("NA") || lg.equals("N/A")) {
                                status = "Failed (" + lg + ")";
                                statusColor = new Color(239, 68, 68);
                            } else {
                                String ay = gr.getAcademicYear();
                                String ayShort = ay;
                                if (ay.length() >= 9) {
                                    ayShort = ay.substring(2, 4) + "-" + ay.substring(7, 9);
                                }
                                String semShort = gr.getSemester().equalsIgnoreCase("Fall") ? "[G]" : "[B]";
                                status = "Passed (" + ayShort + semShort + ")";
                                statusColor = new Color(34, 197, 94);
                            }
                        } else if (isStudentEnrolled(currentUser.getUsername(), course.getCourseCode())) {
                            status = "Taken";
                            statusColor = ACCENT;
                        }

                        JLabel cl = new JLabel("<html><b>" + course.getCourseCode() + " - " + course.getCourseName() + "</b> (" + credit + ")<br/><font color='" + toHex(statusColor) + "'>" + status + "</font></html>");
                        cl.setForeground(TEXT_LIGHT);
                        cl.setFont(APP_FONT.deriveFont(11f));
                        cl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                        coursesBox.add(cl);
                    }
                }
            }
            
            JLabel ectsLabel = new JLabel("Total: " + totalECTS + " ECTS");
            ectsLabel.setFont(APP_FONT.deriveFont(Font.ITALIC, 11f));
            ectsLabel.setForeground(TEXT_MUTED);
            semPanel.add(ectsLabel, BorderLayout.SOUTH);

            semPanel.add(new JScrollPane(coursesBox), BorderLayout.CENTER);
            grid.add(semPanel);
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private JPanel createTranscriptView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_BG);
        JLabel title = new JLabel("Official Transcript of Records");
        title.setFont(APP_FONT.deriveFont(Font.BOLD, 22f));
        title.setForeground(TEXT_LIGHT);
        header.add(title, BorderLayout.WEST);

        double totalGpa = data.calculateGPA(currentUser.getUsername());
        JLabel gpaLabel = new JLabel("Cumulative GPA: " + String.format("%.2f", totalGpa));
        gpaLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 18f));
        gpaLabel.setForeground(ACCENT);
        header.add(gpaLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(DARK_BG);

        Map<String, List<Object>> grouped = new LinkedHashMap<>();
        for (GradeRecord g : data.grades) {
            if (g.getStudentUsername().equals(currentUser.getUsername())) {
                String key = g.getAcademicYear() + " " + g.getSemester();
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(g);
            }
        }
        for (Enrollment e : data.enrollments) {
            if (e.getStudentUsername().equals(currentUser.getUsername())) {
                boolean hasGrade = false;
                for (GradeRecord g : data.grades) {
                    if (g.getStudentUsername().equals(currentUser.getUsername()) && g.getCourseCode().equals(e.getCourseCode())) {
                        hasGrade = true;
                        break;
                    }
                }
                if (!hasGrade) {
                    String key = e.getAcademicYear() + " " + e.getSemester();
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
                }
            }
        }

        for (String key : grouped.keySet()) {
            JPanel termPanel = new JPanel(new BorderLayout());
            termPanel.setBackground(PANEL_BG);
            termPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 10, 0, DARK_BG),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            JLabel termLabel = new JLabel(key);
            termLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 16f));
            termLabel.setForeground(TEXT_LIGHT);
            termPanel.add(termLabel, BorderLayout.NORTH);

            String[] cols = {"Code", "Course Name", "Credit", "Grade", "Points"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            
            double termPoints = 0;
            int termCredits = 0;

            for (Object obj : grouped.get(key)) {
                if (obj instanceof GradeRecord) {
                    GradeRecord g = (GradeRecord) obj;
                    Course c = findCourse(g.getCourseCode());
                    if (c != null) {
                        int cred = Integer.parseInt(c.getCredit());
                        double pts = cred * g.getGradePoint();
                        termPoints += pts;
                        termCredits += cred;
                        model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), g.getLetterGrade(), String.format("%.1f", pts)});
                    }
                } else if (obj instanceof Enrollment) {
                    Enrollment e = (Enrollment) obj;
                    Course c = findCourse(e.getCourseCode());
                    if (c != null) {
                        model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), "-", "-"});
                    }
                }
            }

            JTable table = new JTable(model);
            styleTable(table);
            
            // Center align Credit, Grade, Points
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

            table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width, table.getRowHeight() * table.getRowCount()));
            termPanel.add(new JScrollPane(table), BorderLayout.CENTER);
            
            double termGpa = termCredits > 0 ? termPoints / termCredits : 0;
            JLabel termGpaLabel = new JLabel("Term GPA: " + String.format("%.2f", termGpa));
            termGpaLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 14f));
            termGpaLabel.setForeground(TEXT_MUTED);
            termGpaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            termPanel.add(termGpaLabel, BorderLayout.SOUTH);

            content.add(termPanel);
        }

        panel.add(new JScrollPane(content), BorderLayout.CENTER);

        JButton exportBtn = new JButton("Export as PDF");
        styleButton(exportBtn);
        exportBtn.addActionListener(e -> exportTranscriptToFile());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(DARK_BG);
        footer.add(exportBtn);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSystemConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(DARK_BG);
        
        JPanel card = new JPanel(new GridLayout(3, 2, 10, 10));
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(PANEL_BG_LIGHT, 15), BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        
        JTextField yearField = new JTextField(data.systemConfig.getCurrentYear());
        JComboBox<String> semCombo = new JComboBox<>(new String[]{"Fall", "Spring"});
        semCombo.setSelectedItem(data.systemConfig.getCurrentSemester());
        
        styleTextField(yearField);
        
        JButton saveBtn = new JButton("Update System Time");
        styleButton(saveBtn);
        saveBtn.addActionListener(e -> {
            data.systemConfig.setCurrentYear(yearField.getText().trim());
            data.systemConfig.setCurrentSemester((String) semCombo.getSelectedItem());
            data.saveData();
            JOptionPane.showMessageDialog(this, "System configuration updated!");
            refreshDashboard();
        });

        JLabel l1 = new JLabel("Academic Year:"); l1.setForeground(TEXT_LIGHT);
        JLabel l2 = new JLabel("Semester:"); l2.setForeground(TEXT_LIGHT);

        card.add(l1); card.add(yearField);
        card.add(l2); card.add(semCombo);
        card.add(new JLabel()); card.add(saveBtn);
        
        panel.add(card);
        return panel;
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
