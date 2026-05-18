package ui;

import models.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.formdev.flatlaf.FlatClientProperties;

public class StudentUI extends JPanel {
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
    private final List<String> draftEnrollments = new ArrayList<>();

    public StudentUI(DataStore data, User currentUser, Runnable logoutAction) {
        this.data = data;
        this.currentUser = currentUser;
        this.logoutAction = logoutAction;

        setLayout(new BorderLayout());
        setBackground(DARK_BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(TOP_BAR_BG);
        topBar.setPreferredSize(new Dimension(1000, 50));

        String welcomeText = "Welcome: " + currentUser.getFullName() + " [" + currentUser.getRole() + "]";
        StudentProfile sp = findStudentProfile(currentUser.getUsername());
        if (sp != null) {
            welcomeText += " - Year " + sp.getYear();
        }
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

        tabs.addTab("Course Registration", createEnrollmentPanel());
        tabs.addTab("Grades", createGradesPanel());
        tabs.addTab("Curriculum", createCurriculumView());
        tabs.addTab("Transcript", createTranscriptView());

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        applyTheme(this);
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

    private int getCurrentSemesterNumber(String studentYearStr) {
        try {
            int year = Integer.parseInt(studentYearStr);
            boolean isSpring = data.systemConfig.getCurrentSemester().equalsIgnoreCase("Spring");
            return (year - 1) * 2 + (isSpring ? 2 : 1);
        } catch (NumberFormatException e) {
            return 1;
        }
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

    private void showCourseDetailsPopup(String courseCode) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Course Statistics - " + courseCode, true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Course Statistics - " + courseCode, true);
        } else {
            dialog = new JDialog((Frame) null, "Course Statistics - " + courseCode, true);
        }
        
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
}
