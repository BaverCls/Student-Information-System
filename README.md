# Student Information System

A comprehensive, file-based University Automation System developed in Java using Swing. This desktop application provides role-based access for Administrators, Instructors, and Students to manage academic operations efficiently.

## Features
- **Role-based Dashboards:** Dedicated interfaces for Admins, Instructors, and Students.
- **Academic Management:** Administrators can define departments, update curriculums, and manage course offerings.
- **Instructor Portal:** Instructors can view their assigned courses, track enrolled students, submit grades, and view grade distributions.
- **Student Portal:** Students can enroll in courses, view their real-time curriculum progress, and export high-quality, print-ready PDF transcripts.
- **File-based Persistence:** All data (users, enrollments, grades, courses) is securely stored locally in UTF-8 encoded text files, requiring no external database setup.

## Running the Application
The project requires Java 11 or higher. No external libraries are needed.

1. Clone the repository.
2. Compile the source code:
   ```bash
   javac -encoding UTF-8 -d bin src/*.java src/models/*.java
   ```
3. Run the application:
   ```bash
   java -cp bin UniversityAutomationApp
   ```

*(Alternatively, you can open the project in any Java IDE like IntelliJ IDEA, Eclipse, or VS Code and run `UniversityAutomationApp.java` directly).*

## Default Credentials
The repository includes pre-generated dummy data (200 students, courses, and historical grades) for testing purposes.
- **Admin:** `admin` / `123`
- **Instructor:** `janesmith` / `smith123`
- **Student:** `ahmet.yilmaz` / `user` (or any generated student username with password `user`)
