package br.ufscar.pescd.security;

import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.fixture.UserFixture;
import br.ufscar.pescd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceBlockTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomUserDetailsService customUserDetailsService = new CustomUserDetailsService(userRepository);

    @BeforeEach
    void setUp() {
        reset(userRepository);
    }

    @Test
    @DisplayName("Usuário ativo é carregado habilitado")
    void loadUserByUsername_activeUser_isEnabled() {
        when(userRepository.findByUsername(UserFixture.TARGET_USERNAME))
                .thenReturn(Optional.of(UserFixture.activeTarget()));

        UserDetails details = customUserDetailsService.loadUserByUsername(UserFixture.TARGET_USERNAME);

        assertTrue(details.isEnabled());
    }

    @Test
    @DisplayName("Usuário bloqueado é carregado desabilitado")
    void loadUserByUsername_blockedUser_isDisabled() {
        when(userRepository.findByUsername(UserFixture.TARGET_USERNAME))
                .thenReturn(Optional.of(UserFixture.blockedTarget()));

        UserDetails details = customUserDetailsService.loadUserByUsername(UserFixture.TARGET_USERNAME);

        assertFalse(details.isEnabled());
    }

    @Test
    @DisplayName("Usuário com situação nula é tratado como bloqueado")
    void loadUserByUsername_nullEnabled_isDisabled() {
        when(userRepository.findByUsername(UserFixture.TARGET_USERNAME))
                .thenReturn(Optional.of(UserFixture.user(UserFixture.TARGET_ID, UserFixture.TARGET_USERNAME,
                        UserRole.PROFESSOR, null)));

        UserDetails details = customUserDetailsService.loadUserByUsername(UserFixture.TARGET_USERNAME);

        assertFalse(details.isEnabled());
    }
}
