package models;

public class StudentProfile {
    private String studentId;
    private String fullName;
    private String department;
    private String year;
    private String username;

    public StudentProfile(String studentId, String fullName, String department, String year, String username) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.department = department;
        this.year = year;
        this.username = username;
    }

    public String toFileString() {
        return studentId + "," + fullName + "," + department + "," + year + "," + username;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
