package ui;

import models.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.formdev.flatlaf.FlatClientProperties;

public class AdminUI extends JPanel {
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
    private final Runnable refreshDashboardAction;

    public AdminUI(DataStore data, User currentUser, Runnable logoutAction, Runnable refreshDashboardAction) {
        this.data = data;
        this.currentUser = currentUser;
        this.logoutAction = logoutAction;
        this.refreshDashboardAction = refreshDashboardAction;

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

        tabs.addTab("User Management", createTablePanel("USER"));
        tabs.addTab("Student Management", createTablePanel("STUDENT"));
        tabs.addTab("Course Management", createTablePanel("COURSE"));
        tabs.addTab("Curriculum Management", createAdminCurriculumPanel());
        tabs.addTab("Reports", createReportPanel());
        tabs.addTab("System Config", createSystemConfigPanel());

        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        applyTheme(this);
    }

    private JPanel createTablePanel(String type) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(DARK_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(DARK_BG);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
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
        } else if (type.equals("COURSE")) {
            headers = new String[]{"Code", "Course", "Credit", "Quota", "Semester", "Dept", "Instructor", "Type"};
            model.setColumnIdentifiers(headers);
            for (Course c : data.courses) {
                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getQuota(), c.getYear(), c.getDepartment(), c.getInstructorUsername(), c.getCourseType()});
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
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

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

        return panel;
    }

    private void showAddWindow(String type, DefaultTableModel model) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (type.equals("COURSE")) {
            JTextField t1 = new JTextField();
            JTextField t2 = new JTextField();
            JComboBox<String> c1 = new JComboBox<>(new String[]{"2", "3", "4", "5", "6"});
            JTextField t3 = new JTextField();
            JComboBox<String> cYear = new JComboBox<>(new String[]{"1", "2", "3", "4"});
            JComboBox<String> cDept = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            JTextField t4 = new JTextField();
            JComboBox<String> cType = new JComboBox<>(new String[]{"Mandatory", "Elective", "Technical Elective"});

            Object[] form = { "Code:", t1, "Name:", t2, "Credit:", c1, "Quota:", t3, "Year:", cYear, "Dept:", cDept, "Instructor:", t4, "Type:", cType };
            int res = JOptionPane.showConfirmDialog(parentWindow, form, "Add Course", JOptionPane.OK_CANCEL_OPTION);
            
            if (res == JOptionPane.OK_OPTION
                    && Validator.validateRequiredText(t1.getText(), "Course code")
                    && Validator.validateRequiredText(t2.getText(), "Course name")
                    && Validator.validateQuota(t3.getText())
                    && Validator.validateRequiredText(t4.getText(), "Instructor username")
                    && Validator.isCourseCodeUnique(t1.getText(), data.courses)
                    && isInstructorUsername(t4.getText())) {
                Course c = new Course(t1.getText().trim(), t2.getText().trim(), c1.getSelectedItem().toString(), t3.getText().trim(), t4.getText().trim(), cYear.getSelectedItem().toString(), cDept.getSelectedItem().toString(), cType.getSelectedItem().toString());
                data.courses.add(c);
                data.saveData();
                model.addRow(new Object[]{c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getQuota(), c.getYear(), c.getDepartment(), c.getInstructorUsername(), c.getCourseType()});
                if (refreshDashboardAction != null) refreshDashboardAction.run();
            }
        } else if (type.equals("USER") || type.equals("STUDENT")) {
            JTextField uField = new JTextField();
            JTextField pField = new JTextField();
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "INSTRUCTOR", "STUDENT"});
            JTextField nameField = new JTextField();
            
            JComboBox<String> deptCombo = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            JTextField yearField = new JTextField();

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

            int res = JOptionPane.showConfirmDialog(parentWindow, form, "Add Record", JOptionPane.OK_CANCEL_OPTION);

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

                String autoId = data.generateNextId(role);
                
                User u = new User(uField.getText().trim(), pField.getText().trim(), role, nameField.getText().trim(), autoId);
                data.users.add(u);

                if (role.equals("STUDENT")) {
                    StudentProfile sp = new StudentProfile(autoId, nameField.getText().trim(), (String)deptCombo.getSelectedItem(), yearField.getText().trim(), uField.getText().trim());
                    data.students.add(sp);
                }
                
                data.saveData();

                if (type.equals("USER")) {
                    model.addRow(new Object[]{u.getReferenceId(), u.getUsername(), u.getRole(), u.getFullName(), 
                        role.equals("STUDENT") ? deptCombo.getSelectedItem().toString() : "-",
                        role.equals("STUDENT") ? yearField.getText().trim() : "-"});
                } else if (type.equals("STUDENT") && role.equals("STUDENT")) {
                    StudentProfile sp = findStudentProfile(u.getUsername());
                    if (sp != null) {
                        model.addRow(new Object[]{sp.getStudentId(), sp.getFullName(), sp.getDepartment(), sp.getYear(), sp.getUsername()});
                    }
                }
                
                JOptionPane.showMessageDialog(parentWindow, "Record added successfully! ID: " + autoId);
                if (refreshDashboardAction != null) refreshDashboardAction.run();
            }
        }
    }

    private void showEditWindow(String type, int modelRow, DefaultTableModel model) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
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
            JComboBox<String> cType = new JComboBox<>(new String[]{"Mandatory", "Elective", "Technical Elective"});
            cType.setSelectedItem(c.getCourseType());

            Object[] form = { "Code:", t1, "Name:", t2, "Credit:", c1, "Quota:", t3, "Year:", cYear, "Dept:", cDept, "Instructor:", t4, "Type:", cType };
            if (JOptionPane.showConfirmDialog(parentWindow, form, "Edit Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
                c.setCourseType(cType.getSelectedItem().toString());
                updateCourseCodeReferences(oldCourseCode, newCourseCode);
                data.saveData();
                model.setValueAt(c.getCourseCode(), modelRow, 0);
                model.setValueAt(c.getCourseName(), modelRow, 1);
                model.setValueAt(c.getCredit(), modelRow, 2);
                model.setValueAt(c.getQuota(), modelRow, 3);
                model.setValueAt(c.getYear(), modelRow, 4);
                model.setValueAt(c.getDepartment(), modelRow, 5);
                model.setValueAt(c.getInstructorUsername(), modelRow, 6);
                model.setValueAt(c.getCourseType(), modelRow, 7);
                if (refreshDashboardAction != null) refreshDashboardAction.run();
            }
        } else if (type.equals("USER")) {
            User u = data.users.get(modelRow);
            String oldUsername = u.getUsername();
            JTextField t1 = new JTextField(u.getUsername());
            JTextField t2 = new JTextField(u.getFullName());
            Object[] form = { "Username:", t1, "Name:", t2 };
            if (JOptionPane.showConfirmDialog(parentWindow, form, "Edit User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
                if (refreshDashboardAction != null) refreshDashboardAction.run();
            }
        } else if (type.equals("STUDENT")) {
            String username = (String) model.getValueAt(modelRow, 4);
            StudentProfile s = findStudentProfile(username);
            if (s == null) return;

            JTextField t1 = new JTextField(s.getFullName());
            JComboBox<String> cDept = new JComboBox<>(new String[]{"CE", "EE", "IE", "ME", "BA"});
            cDept.setSelectedItem(s.getDepartment());
            JTextField t3 = new JTextField(s.getYear());
            Object[] form = { "Name:", t1, "Dept:", cDept, "Year:", t3 };
            if (JOptionPane.showConfirmDialog(parentWindow, form, "Edit Student", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
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
                if (refreshDashboardAction != null) refreshDashboardAction.run();
            }
        }
    }

    private void deleteAction(String type, int modelRow, DefaultTableModel model) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        int confirm = JOptionPane.showConfirmDialog(parentWindow, "Are you sure you want to delete this record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
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
                
                for (User u : data.users) {
                    if (u.getUsername().equals(username)) {
                        data.users.remove(u);
                        break;
                    }
                }
            }
            
            data.saveData();
            model.removeRow(modelRow);
            if (refreshDashboardAction != null) refreshDashboardAction.run();
        }
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
                            
                            String courseDisplayName;
                            if (c.getCourseCode().contains("_TECHSEL")) {
                                courseDisplayName = "Technical Elective";
                            } else if (c.getCourseCode().contains("_SEL")) {
                                courseDisplayName = "Elective";
                            } else {
                                courseDisplayName = course.getCourseName();
                            }
                            
                            JLabel cl = new JLabel("<html><b>" + course.getCourseCode() + "</b> - " + courseDisplayName + " (" + course.getCredit() + " ECTS)</html>");
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
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Add Course to " + dept + " Semester " + semester, Dialog.ModalityType.APPLICATION_MODAL);
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

    private JPanel createReportPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 30));
        main.setBackground(DARK_BG);
        main.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel topCards = new JPanel(new GridLayout(1, 3, 20, 0));
        topCards.setBackground(DARK_BG);
        topCards.add(createReportCard("TOTAL USERS", String.valueOf(data.users.size()), new Color(79, 70, 229)));
        topCards.add(createReportCard("STUDENTS", String.valueOf(data.students.size()), new Color(16, 185, 129)));
        topCards.add(createReportCard("COURSES", String.valueOf(data.courses.size()), new Color(245, 158, 11)));
        
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
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "System configuration updated!");
            if (refreshDashboardAction != null) refreshDashboardAction.run();
        });

        JLabel l1 = new JLabel("Academic Year:"); l1.setForeground(TEXT_LIGHT);
        JLabel l2 = new JLabel("Semester:"); l2.setForeground(TEXT_LIGHT);

        card.add(l1); card.add(yearField);
        card.add(l2); card.add(semCombo);
        card.add(new JLabel()); card.add(saveBtn);
        
        panel.add(card);
        return panel;
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

    private void styleTextField(JTextField field) {
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
}
