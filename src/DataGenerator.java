import models.*;
import java.util.*;
import java.text.Normalizer;

public class DataGenerator {
    public static void main(String[] args) {
        DataStore ds = new DataStore();
        
        // Remove existing generated dummy students
        ds.users.removeIf(u -> u.getRole().equals("STUDENT"));
        ds.students.clear();
        ds.grades.clear();
        ds.enrollments.clear();
        
        String[] firstNames = {
            "Ahmet", "Mehmet", "Ayşe", "Fatma", "Zeynep", "Ali", "Can", "Elif", "Burak", "Cem",
            "John", "Emma", "Liam", "Olivia", "Noah", "Sophia", "James", "Isabella", "William", "Mia",
            "Mateo", "Camila", "Lucas", "Valeria", "Diego", "Elena", "Matias", "Lucia", "Gabriel", "Sofia",
            "Hiroshi", "Yuki", "Kenji", "Aiko", "Takumi", "Sakura", "Ren", "Hina", "Sota", "Yui",
            "Vladimir", "Anastasia", "Dmitry", "Maria", "Ivan", "Anna", "Mikhail", "Natalia", "Pavel", "Olga"
        };
        
        String[] lastNames = {
            "Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Yıldırım", "Öztürk", "Aydın", "Özdemir",
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Hernandez", "Lopez", "Gonzalez", "Perez", "Sanchez", "Ramirez", "Torres", "Flores", "Rivera", "Gomez",
            "Sato", "Suzuki", "Takahashi", "Tanaka", "Watanabe", "Ito", "Yamamoto", "Nakamura", "Kobayashi", "Kato",
            "Ivanov", "Smirnov", "Kuznetsov", "Popov", "Vasiliev", "Petrov", "Sokolov", "Mikhailov", "Novikov", "Fedorov"
        };
        
        String[] departments = {"CE", "EE", "IE", "ME", "BA"};
        Random rand = new Random(42);
        
        Set<String> usedUsernames = new HashSet<>();
        
        int globalStudentCount = 1;
        
        for (String dept : departments) {
            List<Curriculum> curr = ds.getCurriculumForDept(dept);
            
            for (int year = 1; year <= 4; year++) {
                // 10 students per year per department -> 40 total
                for (int i = 1; i <= 10; i++) {
                    String fName = firstNames[rand.nextInt(firstNames.length)];
                    String lName = lastNames[rand.nextInt(lastNames.length)];
                    String fullName = fName + " " + lName;
                    
                    // Generate unique username
                    String baseUsername = generateBaseUsername(fName, lName);
                    String username = baseUsername;
                    int counter = 1;
                    while (usedUsernames.contains(username)) {
                        username = baseUsername + counter;
                        counter++;
                    }
                    usedUsernames.add(username);
                    
                    String studentId = "S" + globalStudentCount;
                    
                    // Add User
                    ds.users.add(new User(username, "user", "STUDENT", fullName, studentId));
                    // Add StudentProfile
                    ds.students.add(new StudentProfile(studentId, fullName, dept, String.valueOf(year), username));
                    globalStudentCount++;
                    
                    // Determine which semesters they have completed
                    int completedSemesters = (year - 1) * 2;
                    
                    boolean isFailingStudent = (i == 1 && year > 1);
                    int failedCourseCount = 0;
                    
                    for (Curriculum c : curr) {
                        if (c.getSemester() <= completedSemesters) {
                            int midterm, finalExam;
                            if (isFailingStudent && failedCourseCount < 1) {
                                // Fail this course
                                midterm = 30 + rand.nextInt(15);
                                finalExam = 30 + rand.nextInt(15);
                                failedCourseCount++;
                            } else {
                                // Pass this course
                                midterm = 60 + rand.nextInt(41);
                                finalExam = 60 + rand.nextInt(41);
                            }
                            
                            int pastYear = 2026 - (year - ((c.getSemester()-1)/2 + 1));
                            String academicYearStr = pastYear + "-" + (pastYear + 1);
                            String semesterStr = (c.getSemester() % 2 == 1) ? "Fall" : "Spring";
                            
                            ds.grades.add(new GradeRecord(username, c.getCourseCode(), String.valueOf(midterm), String.valueOf(finalExam), academicYearStr, semesterStr));
                        }
                    }
                }
            }
        }
        
        ds.saveData();
        System.out.println("Dummy data generation completed successfully with real names!");
    }
    
    private static String generateBaseUsername(String fName, String lName) {
        String normalizedFirst = normalize(fName);
        String normalizedLast = normalize(lName);
        return normalizedFirst + "." + normalizedLast;
    }
    
    private static String normalize(String str) {
        // Convert Turkish chars to English equivalents manually first
        str = str.replace("ı", "i").replace("İ", "i")
                 .replace("ğ", "g").replace("Ğ", "g")
                 .replace("ü", "u").replace("Ü", "u")
                 .replace("ş", "s").replace("Ş", "s")
                 .replace("ö", "o").replace("Ö", "o")
                 .replace("ç", "c").replace("Ç", "c");
        
        // Remove other diacritics
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
        return nfdNormalizedString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }
}
