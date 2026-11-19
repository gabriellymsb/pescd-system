package br.ufscar.pescd.fixture;

import br.ufscar.pescd.dto.PublicUserResponse;
import br.ufscar.pescd.dto.UserForm;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.UserRole;

public final class UserFixture {

    public static final String ADMIN_USERNAME = "admin";
    public static final String TARGET_USERNAME = "professor";
    public static final Long TARGET_ID = 2L;

    private UserFixture() {
    }

    public static User admin() {
        return user(1L, ADMIN_USERNAME, UserRole.ADMIN, true);
    }

    public static User activeTarget() {
        return user(TARGET_ID, TARGET_USERNAME, UserRole.PROFESSOR, true);
    }

    public static User blockedTarget() {
        return user(TARGET_ID, TARGET_USERNAME, UserRole.PROFESSOR, false);
    }

    public static User user(Long id, String username, UserRole role, Boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setFullName("Nome " + username);
        user.setEmail(username + "@ufscar.br");
        user.setUsername(username);
        user.setPassword("hash");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }

    public static PublicUserResponse targetPublicResponse(boolean enabled) {
        return new PublicUserResponse(
                TARGET_ID,
                "Nome " + TARGET_USERNAME,
                TARGET_USERNAME + "@ufscar.br",
                TARGET_USERNAME,
                UserRole.PROFESSOR.name(),
                enabled
        );
    }

    public static UserForm targetUpdateForm() {
        UserForm form = new UserForm();
        form.setId(TARGET_ID);
        form.setFullName("Professor Atualizado");
        form.setEmail("professor.atualizado@ufscar.br");
        form.setUsername(TARGET_USERNAME);
        form.setPassword("");
        form.setRole(UserRole.PROFESSOR);
        return form;
    }

    public static UserForm newUserForm() {
        UserForm form = new UserForm();
        form.setFullName("Novo Secretario");
        form.setEmail("secretario@ufscar.br");
        form.setUsername("secretario");
        form.setPassword("senha123");
        form.setRole(UserRole.SECRETARIO);
        return form;
    }
}
