package ie.propertyhub.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Assignment details shown on every page ({@code ${@projectInfo...}} in Thymeleaf) and on /about.
 * Values come from {@code propertyhub.project.*} in application.yml, so they are changed in one place.
 * The version and commit come from the build and prove which release is running.
 */
@Component("projectInfo")
public class ProjectInfo {

    private final String university;
    private final String course;
    private final String assignment;
    private final String studentName;
    private final String studentNumber;
    private final String version;
    private final String commit;

    public ProjectInfo(@Value("${propertyhub.project.university:Atlantic Technological University (ATU)}") String university,
                       @Value("${propertyhub.project.course:Postgraduate DevOps}") String course,
                       @Value("${propertyhub.project.assignment:Task 3 - CI/CD Pipeline}") String assignment,
                       @Value("${propertyhub.project.student-name:Raju Augustine}") String studentName,
                       @Value("${propertyhub.project.student-number:L00205200}") String studentNumber,
                       @Value("${info.app.version:local}") String version,
                       @Value("${info.app.commit:local}") String commit) {
        this.university = university;
        this.course = course;
        this.assignment = assignment;
        this.studentName = studentName;
        this.studentNumber = studentNumber;
        this.version = version;
        this.commit = commit;
    }

    public String getUniversity() {
        return university;
    }

    public String getCourse() {
        return course;
    }

    public String getAssignment() {
        return assignment;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getVersion() {
        return version;
    }

    public String getCommit() {
        return commit;
    }

    /** First 7 characters of the git commit, like GitHub shows it. */
    public String getShortCommit() {
        return commit == null || commit.length() <= 7 ? commit : commit.substring(0, 7);
    }
}
