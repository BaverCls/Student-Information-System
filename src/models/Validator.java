package models;

import javax.swing.*;
import java.util.List;

public class Validator {

    public static boolean isNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            showError(fieldName + " cannot be empty.");
            return false;
        }
        return true;
    }

    public static boolean hasNoComma(String value, String fieldName) {
        if (value != null && value.contains(",")) {
            showError(fieldName + " cannot contain commas (,).");
            return false;
        }
        return true;
    }

    public static boolean validateRequiredText(String value, String fieldName) {
        return isNotEmpty(value, fieldName) && hasNoComma(value, fieldName);
    }

    public static boolean validateRequiredFields(JTextField... fields) {
        for (JTextField field : fields) {
            String fieldName = field.getName() == null ? "Field" : field.getName();
            if (!validateRequiredText(field.getText(), fieldName)) {
                return false;
            }
        }
        return true;
    }

    public static Integer parseInteger(String value, String fieldName) {
        if (!validateRequiredText(value, fieldName)) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showError(fieldName + " must be a valid number.");
            System.err.println("Invalid number for " + fieldName + ": " + value);
            return null;
        }
    }

    public static boolean validateIntegerRange(String value, String fieldName, int min, int max) {
        Integer number = parseInteger(value, fieldName);
        if (number == null) {
            return false;
        }

        if (number < min || number > max) {
            showError(fieldName + " must be between " + min + " and " + max + ".");
            return false;
        }
        return true;
    }

    public static boolean isUsernameUnique(String username, List<User> users) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                showError("This username already exists.");
                return false;
            }
        }
        return true;
    }

    public static boolean isCourseCodeUnique(String courseCode, List<Course> courses) {
        for (Course course : courses) {
            if (course.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                showError("This course code already exists.");
                return false;
            }
        }
        return true;
    }

    public static boolean validateCredit(String value) {
        return validateIntegerRange(value, "Credit", 1, 10);
    }

    public static boolean validateQuota(String value) {
        return validateIntegerRange(value, "Quota", 1, 500);
    }

    public static boolean validateYear(String value) {
        return validateIntegerRange(value, "Year", 1, 8);
    }

    public static boolean validateGrade(String value, String fieldName) {
        return validateIntegerRange(value, fieldName, 0, 100);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
