package models;

public class Enrollment {
    private String studentUsername;
    private String courseCode;
    private String academicYear;
    private String semester;

    public Enrollment(String studentUsername, String courseCode, String academicYear, String semester) {
        this.studentUsername = studentUsername;
        this.courseCode = courseCode;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public String toFileString() {
        return studentUsername + "," + courseCode + "," + academicYear + "," + semester;
    }

    // Getters and Setters
    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
