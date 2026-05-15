package models;

public class Curriculum {
    private String department;
    private int semester;
    private String courseCode;

    public Curriculum(String department, int semester, String courseCode) {
        this.department = department;
        this.semester = semester;
        this.courseCode = courseCode;
    }

    public String toFileString() {
        return department + "," + semester + "," + courseCode;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
}
