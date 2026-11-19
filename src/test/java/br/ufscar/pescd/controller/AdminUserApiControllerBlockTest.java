package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.PublicUserResponse;
import br.ufscar.pescd.fixture.UserFixture;
import br.ufscar.pescd.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class AdminUserApiControllerBlockTest {

    private final UserService userService = mock(UserService.class);
    private final AdminUserApiController controller = new AdminUserApiController(userService);
    private final Authentication authentication =
            new UsernamePasswordAuthenticationToken(UserFixture.ADMIN_USERNAME, null);

    @BeforeEach
    void setUp() {
        reset(userService);
    }

    @Test
    @DisplayName("Bloqueio pela API retorna o usuário atualizado")
    void block_returnsUpdatedUser() {
        PublicUserResponse expected = UserFixture.targetPublicResponse(false);
        when(userService.block(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME)).thenReturn(expected);

        assertSame(expected, controller.block(UserFixture.TARGET_ID, authentication));
    }

    @Test
    @DisplayName("Desbloqueio pela API retorna o usuário atualizado")
    void unblock_returnsUpdatedUser() {
        PublicUserResponse expected = UserFixture.targetPublicResponse(true);
        when(userService.unblock(UserFixture.TARGET_ID, UserFixture.ADMIN_USERNAME)).thenReturn(expected);

        assertSame(expected, controller.unblock(UserFixture.TARGET_ID, authentication));
    }
}
