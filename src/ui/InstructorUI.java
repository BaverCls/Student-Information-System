package ui;

import models.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.formdev.flatlaf.FlatClientProperties;

public class InstructorUI extends JPanel {
    private static final Color DARK_BG = new Color(15, 23, 42); // Deep dark blue
    private static final Color PANEL_BG = new Color(30, 41, 59); // Slightly lighter dark
    private static final Color PANEL_BG_LIGHT = new Color(51, 65, 85); // Borders and highlights
    private static final Color TEXT_LIGHT = new Color(248, 250, 252); // White/Light gray text
    private static final Color TEXT_MUTED = new Color(148, 163, 184); // Muted text
    private static final Color ACCENT = new Color(56, 189, 248); // Bright blue accent
    private static final Color ACCENT_HOVER = new Color(14, 165, 233);
    private static final Color TOP_BAR_BG = new Color(15, 23, 42);
    private static final Font APP_FONT = loadAppFont();

    private final DataStore data;
    private final User currentUser;
    private final Runnable logoutAction;

    public InstructorUI(DataStore data, User currentUser, Runnable logoutAction) {
        this.data = data;
        this.currentUser = currentUser;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(DARK_BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(TOP_BAR_BG);
        topBar.setPreferredSize(new Dimension(1000, 50));

        String welcomeText = "Welcome: " + currentUser.getFullName() + " [" + currentUser.getRole() + "]";
        JLabel welcomeLabel = new JLabel("  " + welcomeText);
        welcomeLabel.setForeground(TEXT_LIGHT);
        welcomeLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 16f));

        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton);
        logoutButton.setBackground(new Color(225, 29, 72)); // Modern soft red

        logoutButton.addActionListener(e -> {
            if (logoutAction != null) {
                logoutAction.run();
            }
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

        tabs.addTab("My Courses", createInstructorCoursesPanel());
        tabs.addTab("Grade Entry", createGradeEntryPanel());
        tabs.addTab("Statistics", createInstructorStatsPanel());

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        applyTheme(this);
    }

    private JPanel createInstructorCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);

        JPanel topHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topHeader.setBackground(DARK_BG);
        JLabel titleLabel = new JLabel("My Assigned Courses");
        titleLabel.setFont(APP_FONT.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(TEXT_LIGHT);
        topHeader.add(titleLabel);
        panel.add(topHeader, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        String[] headers = new String[]{"Code", "Course", "Credit", "Quota", "Semester", "Enrolled", "Type"};
        model.setColumnIdentifiers(headers);
        for (Course c : data.courses) {
            if (c.getInstructorUsername().equals(currentUser.getUsername())) {
                model.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredit(),
                    c.getQuota(),
                    c.getYear(),
                    countEnrollmentsForCourse(c.getCourseCode()),
                    c.getCourseType()
                });
            }
        }

        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

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

        return panel;
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
        saveButton.addActionListener(e -> saveGradesFromTable(model, courseCombo));

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

    private void saveGradesFromTable(DefaultTableModel model, JComboBox<String> courseCombo) {
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
        
        // Refresh local table values
        String selected = (String) courseCombo.getSelectedItem();
        if (selected != null) {
            fillGradeEntryModel(model, getCourseCodeFromOption(selected));
        }
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

    private GradeRecord findGradeRecord(String studentUsername, String courseCode) {
        GradeRecord latest = null;
        for (GradeRecord grade : data.grades) {
            if (grade.getStudentUsername().equals(studentUsername) && grade.getCourseCode().equals(courseCode)) {
                latest = grade;
            }
        }
        return latest;
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

    private class GradeChartPanel extends JPanel {
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
}
