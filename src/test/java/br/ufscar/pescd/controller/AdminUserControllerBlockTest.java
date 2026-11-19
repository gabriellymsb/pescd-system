package br.ufscar.pescd.controller;

import br.ufscar.pescd.exception.BusinessRuleException;
import br.ufscar.pescd.fixture.UserFixture;
import br.ufscar.pescd.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserControllerBlockTest {

    private static final String SELF_ERROR = "Você não pode bloquear ou desbloquear seu próprio usuário.";

    private final UserService userService = mock(UserService.class);
    private final AdminUserController controller = new AdminUserController(userService);
    private final Authentication authentication =
            new UsernamePasswordAuthenticationToken(UserFixture.ADMIN_USERNAME, null);

    private RedirectAttributesModelMap redirectAttributes;

    @BeforeEach
    void setUp() {
        reset(userService);
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("Bloqueia usuário e redireciona com mensagem de sucesso")
    void block_success_redirectsWithSuccessMessage() {
        String view = controller.block(UserFixture.TARGET_ID, authentication, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("Usuário bloqueado com sucesso.", redirectAttributes.getFlashAttributes().get("successMessage"));
        verify(userService).block(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME);
    }

    @Test
    @DisplayName("Erro ao bloquear redireciona com mensagem de erro")
    void block_businessRule_redirectsWithErrorMessage() {
        when(userService.block(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME))
                .thenThrow(new BusinessRuleException(SELF_ERROR));

        String view = controller.block(UserFixture.TARGET_ID, authentication, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals(SELF_ERROR, redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    @DisplayName("Desbloqueia usuário e redireciona com mensagem de sucesso")
    void unblock_success_redirectsWithSuccessMessage() {
        String view = controller.unblock(UserFixture.TARGET_ID, authentication, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals("Usuário desbloqueado com sucesso.", redirectAttributes.getFlashAttributes().get("successMessage"));
        verify(userService).unblock(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME);
    }

    @Test
    @DisplayName("Erro ao desbloquear redireciona com mensagem de erro")
    void unblock_businessRule_redirectsWithErrorMessage() {
        when(userService.unblock(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME))
                .thenThrow(new BusinessRuleException(SELF_ERROR));

        String view = controller.unblock(UserFixture.TARGET_ID, authentication, redirectAttributes);

        assertEquals("redirect:/admin/users", view);
        assertEquals(SELF_ERROR, redirectAttributes.getFlashAttributes().get("errorMessage"));
    }
}
