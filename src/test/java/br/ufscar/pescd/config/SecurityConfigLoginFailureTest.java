package br.ufscar.pescd.config;

import br.ufscar.pescd.security.CustomUserDetailsService;
import br.ufscar.pescd.security.RoleBasedAuthenticationSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SecurityConfigLoginFailureTest {

    private final SecurityConfig securityConfig = new SecurityConfig(
            mock(CustomUserDetailsService.class),
            mock(RoleBasedAuthenticationSuccessHandler.class)
    );
    private final AuthenticationFailureHandler failureHandler = securityConfig.loginFailureHandler();

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("POST", "/login");
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Usuário bloqueado é redirecionado para o login com aviso de bloqueio")
    void disabledUser_redirectsToBlockedMessage() throws Exception {
        failureHandler.onAuthenticationFailure(request, response, new DisabledException("User is disabled"));

        assertEquals("/login?blocked", response.getRedirectedUrl());
    }

    @Test
    @DisplayName("Credenciais inválidas mantêm o aviso genérico de erro")
    void badCredentials_redirectsToGenericError() throws Exception {
        failureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("Bad credentials"));

        assertEquals("/login?error", response.getRedirectedUrl());
    }
}
