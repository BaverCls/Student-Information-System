package models;

public class SystemConfig {
    private String currentYear;
    private String currentSemester;

    public SystemConfig(String currentYear, String currentSemester) {
        this.currentYear = currentYear;
        this.currentSemester = currentSemester;
    }

    public String toFileString() {
        return currentYear + "," + currentSemester;
    }

    public String getCurrentYear() { return currentYear; }
    public void setCurrentYear(String currentYear) { this.currentYear = currentYear; }
    public String getCurrentSemester() { return currentSemester; }
    public void setCurrentSemester(String currentSemester) { this.currentSemester = currentSemester; }
}
