package br.ufscar.pescd.fixture;

import br.ufscar.pescd.entity.Documentation;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.Report;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.entity.WorkPlan;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class ProfessorResponsavelDownloadFixture {

    public static final Long OFFER_ID = 10L;
    public static final Long STUDENT_ID = 20L;
    public static final String USERNAME = "responsavel";
    public static final String CONTENT_TYPE = "application/pdf";
    public static final String WORK_PLAN_FILE_NAME = "plano.pdf";
    public static final String WORK_PLAN_FID = "1,plano";
    public static final String REPORT_FILE_NAME = "relatorio.pdf";
    public static final String REPORT_FID = "2,relatorio";
    public static final String DOCUMENTATION_FILE_NAME = "documentacao.pdf";
    public static final String DOCUMENTATION_FID = "3,documentacao";
    public static final byte[] FILE_CONTENT = {37, 80, 68, 70};

    private ProfessorResponsavelDownloadFixture() {
    }

    public static Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(USERNAME, null);
    }

    public static User professor() {
        User professor = new User();
        professor.setId(1L);
        professor.setUsername(USERNAME);
        return professor;
    }

    public static OfferStudent enrollment() {
        OfferStudent enrollment = new OfferStudent();
        enrollment.setId(STUDENT_ID);
        return enrollment;
    }

    public static WorkPlan workPlan() {
        WorkPlan workPlan = new WorkPlan();
        workPlan.setFileName(WORK_PLAN_FILE_NAME);
        workPlan.setContentType(CONTENT_TYPE);
        workPlan.setFileFid(WORK_PLAN_FID);
        return workPlan;
    }

    public static Report report() {
        Report report = new Report();
        report.setFileName(REPORT_FILE_NAME);
        report.setContentType(CONTENT_TYPE);
        report.setFileFid(REPORT_FID);
        return report;
    }

    public static Documentation documentation() {
        Documentation documentation = new Documentation();
        documentation.setFileName(DOCUMENTATION_FILE_NAME);
        documentation.setContentType(CONTENT_TYPE);
        documentation.setFileFid(DOCUMENTATION_FID);
        return documentation;
    }
}
