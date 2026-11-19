package br.ufscar.pescd.controller;

import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.ProfessorResponsavelService;
import br.ufscar.pescd.storage.SeaweedFsStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.DOCUMENTATION_FID;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.DOCUMENTATION_FILE_NAME;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.FILE_CONTENT;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.OFFER_ID;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.REPORT_FID;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.REPORT_FILE_NAME;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.STUDENT_ID;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.USERNAME;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.WORK_PLAN_FID;
import static br.ufscar.pescd.fixture.ProfessorResponsavelDownloadFixture.WORK_PLAN_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class ProfessorResponsavelApiControllerTest {

    private final ProfessorResponsavelService professorResponsavelService = mock(ProfessorResponsavelService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SeaweedFsStorageService storageService = mock(SeaweedFsStorageService.class);
    private final ProfessorResponsavelApiController controller =
            new ProfessorResponsavelApiController(professorResponsavelService, userRepository, storageService);

    private final User professor = ProfessorResponsavelDownloadFixture.professor();
    private final OfferStudent enrollment = ProfessorResponsavelDownloadFixture.enrollment();

    @BeforeEach
    void setUp() {
        reset(professorResponsavelService, userRepository, storageService);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(professor));
        when(professorResponsavelService.getEnrollmentForResponsavel(OFFER_ID, STUDENT_ID, professor))
                .thenReturn(enrollment);
    }

    @Test
    void downloadWorkPlanReturnsInlinePdf() {
        when(professorResponsavelService.getWorkPlan(enrollment))
                .thenReturn(ProfessorResponsavelDownloadFixture.workPlan());
        when(storageService.read(WORK_PLAN_FID)).thenReturn(FILE_CONTENT);

        ResponseEntity<byte[]> response = controller.downloadWorkPlan(
                OFFER_ID, STUDENT_ID, ProfessorResponsavelDownloadFixture.authentication());

        assertInlinePdf(response, WORK_PLAN_FILE_NAME);
    }

    @Test
    void downloadReportReturnsInlinePdf() {
        when(professorResponsavelService.getReport(enrollment))
                .thenReturn(ProfessorResponsavelDownloadFixture.report());
        when(storageService.read(REPORT_FID)).thenReturn(FILE_CONTENT);

        ResponseEntity<byte[]> response = controller.downloadReport(
                OFFER_ID, STUDENT_ID, ProfessorResponsavelDownloadFixture.authentication());

        assertInlinePdf(response, REPORT_FILE_NAME);
    }

    @Test
    void downloadDocumentationReturnsInlinePdf() {
        when(professorResponsavelService.getDocumentation(enrollment))
                .thenReturn(ProfessorResponsavelDownloadFixture.documentation());
        when(storageService.read(DOCUMENTATION_FID)).thenReturn(FILE_CONTENT);

        ResponseEntity<byte[]> response = controller.downloadDocumentation(
                OFFER_ID, STUDENT_ID, ProfessorResponsavelDownloadFixture.authentication());

        assertInlinePdf(response, DOCUMENTATION_FILE_NAME);
    }

    private void assertInlinePdf(ResponseEntity<byte[]> response, String fileName) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"" + fileName + "\"");
        assertThat(response.getBody()).isEqualTo(FILE_CONTENT);
    }
}
