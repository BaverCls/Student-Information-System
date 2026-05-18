package models;

public class Course {
    private String courseCode;
    private String courseName;
    private String credit;
    private String quota;
    private String instructorUsername;
    private String year;
    private String department;
    private String courseType; // "Mandatory", "Elective", "Technical Elective"

    public Course(String courseCode, String courseName, String credit, String quota, String instructorUsername, String year, String department) {
        this(courseCode, courseName, credit, quota, instructorUsername, year, department, "Mandatory");
    }

    public Course(String courseCode, String courseName, String credit, String quota, String instructorUsername, String year, String department, String courseType) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.quota = quota;
        this.instructorUsername = instructorUsername;
        this.year = year;
        this.department = department;
        this.courseType = courseType != null ? courseType : "Mandatory";
    }

    public String toFileString() {
        return courseCode + "," + courseName + "," + credit + "," + quota + "," + instructorUsername + "," + year + "," + department + "," + courseType;
    }

    // Getters and Setters
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCredit() { return credit; }
    public void setCredit(String credit) { this.credit = credit; }
    public String getQuota() { return quota; }
    public void setQuota(String quota) { this.quota = quota; }
    public String getInstructorUsername() { return instructorUsername; }
    public void setInstructorUsername(String instructorUsername) { this.instructorUsername = instructorUsername; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
}
