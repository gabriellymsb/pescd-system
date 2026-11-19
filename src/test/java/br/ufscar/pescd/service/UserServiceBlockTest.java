package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.PublicUserResponse;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.exception.BusinessRuleException;
import br.ufscar.pescd.fixture.UserFixture;
import br.ufscar.pescd.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceBlockTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepository, passwordEncoder);

    @BeforeEach
    void setUp() {
        reset(userRepository, passwordEncoder);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Bloqueia um usuário ativo")
    void block_activeUser_disablesUser() {
        User target = UserFixture.activeTarget();
        when(userRepository.findById(UserFixture.TARGET_ID)).thenReturn(Optional.of(target));

        PublicUserResponse response = userService.block(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME);

        assertFalse(target.getEnabled());
        assertFalse(response.enabled());
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("Desbloqueia um usuário bloqueado")
    void unblock_blockedUser_enablesUser() {
        User target = UserFixture.blockedTarget();
        when(userRepository.findById(UserFixture.TARGET_ID)).thenReturn(Optional.of(target));

        PublicUserResponse response = userService.unblock(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME);

        assertTrue(target.getEnabled());
        assertTrue(response.enabled());
        verify(userRepository).save(target);
    }

    @Test
    @DisplayName("Não permite bloquear o próprio usuário")
    void block_ownUser_throwsBusinessRuleException() {
        User admin = UserFixture.admin();
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> userService.block(admin.getId(), UserFixture.ADMIN_USERNAME));

        assertEquals("Você não pode bloquear ou desbloquear seu próprio usuário.", ex.getMessage());
        assertTrue(admin.getEnabled());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Não permite desbloquear o próprio usuário")
    void unblock_ownUser_throwsBusinessRuleException() {
        User admin = UserFixture.admin();
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThrows(BusinessRuleException.class,
                () -> userService.unblock(admin.getId(), UserFixture.ADMIN_USERNAME));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Bloquear usuário inexistente lança EntityNotFoundException")
    void block_unknownUser_throwsEntityNotFound() {
        when(userRepository.findById(UserFixture.TARGET_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.block(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Editar um usuário bloqueado mantém o bloqueio")
    void update_blockedUser_keepsUserBlocked() {
        User target = UserFixture.blockedTarget();
        when(userRepository.findById(UserFixture.TARGET_ID)).thenReturn(Optional.of(target));

        userService.update(UserFixture.targetUpdateForm());

        assertFalse(target.getEnabled());
    }

    @Test
    @DisplayName("Editar um usuário bloqueado pela API mantém o bloqueio")
    void updateFromApi_blockedUser_keepsUserBlocked() {
        User target = UserFixture.blockedTarget();
        when(userRepository.findById(UserFixture.TARGET_ID)).thenReturn(Optional.of(target));

        PublicUserResponse response = userService.updateFromApi(UserFixture.targetUpdateForm());

        assertFalse(response.enabled());
    }

    @Test
    @DisplayName("Usuário novo é criado desbloqueado")
    void create_newUser_isEnabled() {
        when(passwordEncoder.encode("senha123")).thenReturn("hash");

        PublicUserResponse response = userService.createFromApi(UserFixture.newUserForm());

        assertTrue(response.enabled());
    }
}
