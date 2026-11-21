package ie.propertyhub.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProjectInfoTest {

    @Test
    void exposesAssignmentDetailsAndShortCommit() {
        ProjectInfo info = new ProjectInfo("Atlantic Technological University (ATU)", "Postgraduate DevOps",
                "Task 3 - CI/CD Pipeline", "Raju Augustine", "L00205200", "sha-abc", "0123456789abcdef");
        assertThat(info.getStudentNumber()).isEqualTo("L00205200");
        assertThat(info.getUniversity()).contains("ATU");
        assertThat(info.getCourse()).isEqualTo("Postgraduate DevOps");
        assertThat(info.getAssignment()).startsWith("Task 3");
        assertThat(info.getStudentName()).isEqualTo("Raju Augustine");
        assertThat(info.getVersion()).isEqualTo("sha-abc");
        assertThat(info.getShortCommit()).isEqualTo("0123456");
    }

    @Test
    void shortCommitKeepsShortValues() {
        ProjectInfo info = new ProjectInfo("u", "c", "a", "n", "s", "v", "local");
        assertThat(info.getShortCommit()).isEqualTo("local");
    }
}
