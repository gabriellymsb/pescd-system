package br.ufscar.pescd.fixture;

import br.ufscar.pescd.dto.UserForm;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.UserRole;

public final class UserFormFixture {

    public static final Long EXISTING_USER_ID = 10L;
    public static final String FULL_NAME = "Maria Professora";
    public static final String EMAIL = "maria.professora@ufscar.br";
    public static final String USERNAME = "maria.professora";
    public static final String VALID_PASSWORD = "senha123";
    public static final String MIN_LENGTH_PASSWORD = "123456";
    public static final String SHORT_PASSWORD = "12345";
    public static final String ENCODED_PASSWORD = "{bcrypt}senha-codificada";

    private UserFormFixture() {
    }

    public static UserForm creationForm() {
        UserForm form = new UserForm();
        form.setFullName(FULL_NAME);
        form.setEmail(EMAIL);
        form.setUsername(USERNAME);
        form.setPassword(VALID_PASSWORD);
        form.setRole(UserRole.PROFESSOR);
        return form;
    }

    public static UserForm creationFormWithPassword(String password) {
        UserForm form = creationForm();
        form.setPassword(password);
        return form;
    }

    public static UserForm creationFormWithRole(UserRole role) {
        UserForm form = creationForm();
        form.setRole(role);
        return form;
    }

    public static UserForm creationFormViolatingAllRules() {
        UserForm form = creationFormWithRole(UserRole.ALUNO);
        form.setPassword(SHORT_PASSWORD);
        return form;
    }

    public static UserForm editionForm() {
        UserForm form = creationForm();
        form.setId(EXISTING_USER_ID);
        form.setPassword(null);
        return form;
    }

    public static UserForm editionFormWithoutId() {
        UserForm form = editionForm();
        form.setId(null);
        return form;
    }

    public static UserForm editionFormWithPassword(String password) {
        UserForm form = editionForm();
        form.setPassword(password);
        return form;
    }

    public static UserForm editionFormWithRole(UserRole role) {
        UserForm form = editionForm();
        form.setRole(role);
        return form;
    }

    public static User existingUser() {
        User user = new User();
        user.setId(EXISTING_USER_ID);
        user.setFullName("Nome Antigo");
        user.setEmail("antigo@ufscar.br");
        user.setUsername("antigo");
        user.setPassword(ENCODED_PASSWORD);
        user.setRole(UserRole.SECRETARIO);
        user.setEnabled(true);
        return user;
    }
}
