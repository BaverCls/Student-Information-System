import models.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class DataStore {
    private final File dataDirectory = resolveDataDirectory();
    
    public List<User> users = new ArrayList<>();
    public List<StudentProfile> students = new ArrayList<>();
    public List<Course> courses = new ArrayList<>();
    public List<Enrollment> enrollments = new ArrayList<>();
    public List<GradeRecord> grades = new ArrayList<>();
    public List<Curriculum> curriculums = new ArrayList<>();
    public SystemConfig systemConfig = new SystemConfig("2026-2027", "Fall");

    public DataStore() {
        loadData();
    }

    public void loadData() {
        readUsers();
        readStudents();
        readCourses();
        readEnrollments();
        readGrades();
        readCurriculums();
        readSystemConfig();
    }

    private void readUsers() {
        users.clear();
        File file = dataFile("users.txt");
        
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = cleanParts(line.split(","));
                    if (p.length == 5) users.add(new User(p[0], p[1], p[2], p[3], p[4]));
                }
            } catch (IOException e) {
                showFileError("Could not read users.txt", e);
            }
        }

        boolean adminExists = false;
        for (User u : users) {
            if (u.getRole().equals("ADMIN")) {
                adminExists = true;
                break;
            }
        }

        if (!adminExists) {
            File adminFile = dataFile("admin.txt");
            if (adminFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(adminFile), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line = br.readLine();
                    if (line != null) {
                        String[] p = cleanParts(line.split(","));
                        if (p.length == 2) {
                            users.add(new User(p[0], p[1], "ADMIN", "System Administrator", "A1"));
                            saveData();
                        }
                    }
                } catch (IOException e) {
                    showFileError("Could not read admin.txt", e);
                }
            }
        }
    }

    private void readStudents() {
        students.clear();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile("students.txt")), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                String[] p = cleanParts(line.split(","));
                if (p.length == 5) students.add(new StudentProfile(p[0], p[1], p[2], p[3], p[4]));
            }
        } catch (IOException e) {
            showFileError("Could not read students.txt", e);
        }
    }

    private void readCourses() {
        courses.clear();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile("courses.txt")), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = cleanParts(line.split(","));
                if (p.length == 5) {
                    courses.add(new Course(p[0], p[1], p[2], p[3], p[4], "1", inferDept(p[0])));
                } else if (p.length == 6) {
                    courses.add(new Course(p[0], p[1], p[2], p[3], p[4], p[5], inferDept(p[0])));
                } else if (p.length == 7) {
                    courses.add(new Course(p[0], p[1], p[2], p[3], p[4], p[5], p[6]));
                }
            }
        } catch (IOException e) {
            showFileError("Could not read courses.txt", e);
        }
    }

    private String inferDept(String code) {
        if (code.startsWith("CS") || code.startsWith("CE")) return "CE";
        if (code.startsWith("EE")) return "EE";
        if (code.startsWith("IE")) return "IE";
        if (code.startsWith("ME")) return "ME";
        if (code.startsWith("BA")) return "BA";
        return "CE";
    }

    private void readEnrollments() {
        enrollments.clear();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile("enrollments.txt")), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = cleanParts(line.split(","));
                if (p.length == 2) {
                    enrollments.add(new Enrollment(p[0], p[1], systemConfig.getCurrentYear(), systemConfig.getCurrentSemester()));
                } else if (p.length == 4) {
                    enrollments.add(new Enrollment(p[0], p[1], p[2], p[3]));
                }
            }
        } catch (IOException e) {
            showFileError("Could not read enrollments.txt", e);
        }
    }

    private void readGrades() {
        grades.clear();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile("grades.txt")), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = cleanParts(line.split(","));
                if (p.length == 4) {
                    // Legacy format support: assign current system config year/semester
                    grades.add(new GradeRecord(p[0], p[1], p[2], p[3], systemConfig.getCurrentYear(), systemConfig.getCurrentSemester()));
                } else if (p.length == 6) {
                    grades.add(new GradeRecord(p[0], p[1], p[2], p[3], p[4], p[5]));
                }
            }
        } catch (IOException e) {
            showFileError("Could not read grades.txt", e);
        }
    }

    private void readCurriculums() {
        curriculums.clear();
        File file = dataFile("curriculums.txt");
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = cleanParts(line.split(","));
                if (p.length == 3) curriculums.add(new Curriculum(p[0], Integer.parseInt(p[1]), p[2]));
            }
        } catch (IOException e) {
            showFileError("Could not read curriculums.txt", e);
        }
    }

    private void readSystemConfig() {
        File file = dataFile("system_config.txt");
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                String[] p = cleanParts(line.split(","));
                if (p.length == 2) systemConfig = new SystemConfig(p[0], p[1]);
            }
        } catch (IOException e) {
            showFileError("Could not read system_config.txt", e);
        }
    }

    public void saveData() {
        writeToFile("users.txt", "users");
        writeToFile("students.txt", "students");
        writeToFile("courses.txt", "courses");
        writeToFile("enrollments.txt", "enrollments");
        writeToFile("grades.txt", "grades");
        writeToFile("curriculums.txt", "curriculums");
        writeToFile("system_config.txt", "config");
    }

    /**
     * Logic for Auto-ID Generation 
     * Finds the next available ID for a given role by scanning existing users.
     */
    public String generateNextId(String role) {
        String prefix = "";
        if (role.equals("STUDENT")) prefix = "S";
        else if (role.equals("INSTRUCTOR")) prefix = "T";
        else if (role.equals("ADMIN")) prefix = "A";
        else prefix = "U";

        int max = 0;
        for (User u : users) {
            if (u.getReferenceId().startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(u.getReferenceId().substring(1));
                    if (num > max) max = num;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid reference ID format: " + u.getReferenceId());
                }
            }
        }
        return prefix + (max + 1);
    }

    private void writeToFile(String fileName, String type) {
        File targetFile = dataFile(fileName);
        backupFile(targetFile);

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetFile), java.nio.charset.StandardCharsets.UTF_8))) {
            if (type.equals("users")) {
                for (User u : users) pw.println(u.toFileString());
            } else if (type.equals("students")) {
                for (StudentProfile s : students) pw.println(s.toFileString());
            } else if (type.equals("courses")) {
                for (Course c : courses) pw.println(c.toFileString());
            } else if (type.equals("enrollments")) {
                for (Enrollment e : enrollments) pw.println(e.toFileString());
            } else if (type.equals("grades")) {
                for (GradeRecord g : grades) pw.println(g.toFileString());
            } else if (type.equals("curriculums")) {
                for (Curriculum c : curriculums) pw.println(c.toFileString());
            } else if (type.equals("config")) {
                pw.println(systemConfig.toFileString());
            }
        } catch (IOException e) {
            showFileError("Could not save " + fileName, e);
        }
    }

    private File dataFile(String fileName) {
        return new File(dataDirectory, fileName);
    }

    private String[] cleanParts(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace("\uFEFF", "").trim();
        }
        return parts;
    }

    private File resolveDataDirectory() {
        File currentDirData = new File("data");
        if (currentDirData.exists()) return currentDirData;

        File parentDirData = new File("..", "data");
        if (parentDirData.exists()) return parentDirData;

        currentDirData.mkdirs();
        return currentDirData;
    }

    private void backupFile(File sourceFile) {
        if (!sourceFile.exists()) {
            return;
        }

        File backupFile = new File(sourceFile.getParentFile(), sourceFile.getName() + ".bak");

        try (
            FileInputStream input = new FileInputStream(sourceFile);
            FileOutputStream output = new FileOutputStream(backupFile)
        ) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            showFileError("Could not create backup for " + sourceFile.getName(), e);
        }
    }

    private void showFileError(String message, IOException e) {
        System.err.println(message);
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, message + "\n" + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
    }

    // GPA Calculation Logic: Only counts the most recent attempt for each course
    public double calculateGPA(String username) {
        Map<String, GradeRecord> latestGrades = new HashMap<>();
        for (GradeRecord g : grades) {
            if (g.getStudentUsername().equals(username)) {
                // Assuming newer grades are added to the end of the list
                // For better accuracy, we could compare AcademicYear/Semester, but latest-in-list is a common pattern here.
                latestGrades.put(g.getCourseCode(), g);
            }
        }

        double totalPoints = 0;
        int totalCredits = 0;
        for (GradeRecord g : latestGrades.values()) {
            Course c = findCourse(g.getCourseCode());
            if (c != null) {
                try {
                    int credit = Integer.parseInt(c.getCredit());
                    totalPoints += g.getGradePoint() * credit;
                    totalCredits += credit;
                } catch (NumberFormatException ignored) {}
            }
        }
        return totalCredits == 0 ? 0 : totalPoints / totalCredits;
    }

    public Course findCourse(String code) {
        for (Course c : courses) if (c.getCourseCode().equals(code)) return c;
        return null;
    }

    public List<Curriculum> getCurriculumForDept(String dept) {
        List<Curriculum> list = new ArrayList<>();
        for (Curriculum c : curriculums) if (c.getDepartment().equals(dept)) list.add(c);
        return list;
    }
}
