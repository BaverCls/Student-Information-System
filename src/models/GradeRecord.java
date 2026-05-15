package models;

public class GradeRecord {
    private String studentUsername;
    private String courseCode;
    private String midterm;
    private String finalExam;
    private String academicYear;
    private String semester;

    public GradeRecord(String studentUsername, String courseCode, String midterm, String finalExam, String academicYear, String semester) {
        this.studentUsername = studentUsername;
        this.courseCode = courseCode;
        this.midterm = midterm;
        this.finalExam = finalExam;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public String toFileString() {
        return studentUsername + "," + courseCode + "," + midterm + "," + finalExam + "," + academicYear + "," + semester;
    }

    public String getLetterGrade() {
        try {
            double m = Double.parseDouble(midterm);
            double f = Double.parseDouble(finalExam);
            double score = (m * 0.4) + (f * 0.6);
            if (score >= 90) return "AA";
            if (score >= 85) return "BA";
            if (score >= 80) return "BB";
            if (score >= 75) return "CB";
            if (score >= 70) return "CC";
            if (score >= 65) return "DC";
            if (score >= 60) return "DD";
            if (score >= 50) return "FD";
            return "FF";
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }

    public double getGradePoint() {
        String lg = getLetterGrade();
        switch (lg) {
            case "AA": return 4.0;
            case "BA": return 3.5;
            case "BB": return 3.0;
            case "CB": return 2.5;
            case "CC": return 2.0;
            case "DC": return 1.5;
            case "DD": return 1.0;
            case "FD": return 0.5;
            case "FF": return 0.0;
            default: return 0.0;
        }
    }

    // Getters and Setters
    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getMidterm() { return midterm; }
    public void setMidterm(String midterm) { this.midterm = midterm; }
    public String getFinalExam() { return finalExam; }
    public void setFinalExam(String finalExam) { this.finalExam = finalExam; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
