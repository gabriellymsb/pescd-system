package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.ApiErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.DisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiExceptionHandlerBlockedUserTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("Login de usuário bloqueado retorna 403 com mensagem significativa")
    void handleDisabled_returnsForbiddenWithBlockedMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleDisabled(new DisabledException("User is disabled"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Usuário bloqueado. Procure o administrador do sistema.", response.getBody().message());
        assertEquals("/api/auth/login", response.getBody().path());
    }
}
