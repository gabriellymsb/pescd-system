package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.PublicUserResponse;
import br.ufscar.pescd.dto.UserForm;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.exception.DuplicateResourceException;
import br.ufscar.pescd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

import static br.ufscar.pescd.fixture.UserFormFixture.EMAIL;
import static br.ufscar.pescd.fixture.UserFormFixture.ENCODED_PASSWORD;
import static br.ufscar.pescd.fixture.UserFormFixture.EXISTING_USER_ID;
import static br.ufscar.pescd.fixture.UserFormFixture.MIN_LENGTH_PASSWORD;
import static br.ufscar.pescd.fixture.UserFormFixture.SHORT_PASSWORD;
import static br.ufscar.pescd.fixture.UserFormFixture.USERNAME;
import static br.ufscar.pescd.fixture.UserFormFixture.VALID_PASSWORD;
import static br.ufscar.pescd.fixture.UserFormFixture.creationForm;
import static br.ufscar.pescd.fixture.UserFormFixture.creationFormViolatingAllRules;
import static br.ufscar.pescd.fixture.UserFormFixture.creationFormWithPassword;
import static br.ufscar.pescd.fixture.UserFormFixture.creationFormWithRole;
import static br.ufscar.pescd.fixture.UserFormFixture.editionForm;
import static br.ufscar.pescd.fixture.UserFormFixture.editionFormWithPassword;
import static br.ufscar.pescd.fixture.UserFormFixture.editionFormWithRole;
import static br.ufscar.pescd.fixture.UserFormFixture.editionFormWithoutId;
import static br.ufscar.pescd.fixture.UserFormFixture.existingUser;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceValidationTest {

    private static final String DUPLICATE_EMAIL_MESSAGE = "Já existe um usuário com este e-mail.";
    private static final String DUPLICATE_USERNAME_MESSAGE = "Já existe um usuário com este nome de usuário.";
    private static final String PASSWORD_REQUIRED_MESSAGE = "A senha é obrigatória.";
    private static final String PASSWORD_TOO_SHORT_MESSAGE = "A senha deve ter pelo menos 6 caracteres.";
    private static final String INVALID_ROLE_MESSAGE = "O perfil deve ser Administrador, Secretário ou Professor.";

    private static final UserRepository userRepository = mock(UserRepository.class);
    private static final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private static final UserService userService = new UserService(userRepository, passwordEncoder);

    @BeforeEach
    void setUp() {
        reset(userRepository, passwordEncoder);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
    }

    private static BindingResult bindingResultFor(UserForm form) {
        return new BeanPropertyBindingResult(form, "userForm");
    }

    private static void assertFieldError(FieldError error, String field, String code, String message) {
        assertNotNull(error);
        assertAll(
                () -> assertEquals(field, error.getField()),
                () -> assertEquals(code, error.getCode()),
                () -> assertEquals(message, error.getDefaultMessage())
        );
    }

    @Nested
    class ValidateBusinessRulesOnCreation {

        @Test
        void acceptsValidForm() {
            UserForm form = creationForm();
            BindingResult bindingResult = bindingResultFor(form);

            assertTrue(userService.validateBusinessRules(form, bindingResult, false));
            assertFalse(bindingResult.hasErrors());
            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository).existsByUsername(USERNAME);
            verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
            verify(userRepository, never()).existsByUsernameAndIdNot(anyString(), anyLong());
        }

        @Test
        void rejectsDuplicateEmail() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            UserForm form = creationForm();
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("email"), "email", "duplicate", DUPLICATE_EMAIL_MESSAGE);
        }

        @Test
        void rejectsDuplicateUsername() {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
            UserForm form = creationForm();
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("username"), "username", "duplicate", DUPLICATE_USERNAME_MESSAGE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsMissingPassword(String password) {
            UserForm form = creationFormWithPassword(password);
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("password"), "password", "required", PASSWORD_REQUIRED_MESSAGE);
        }

        @Test
        void rejectsShortPassword() {
            UserForm form = creationFormWithPassword(SHORT_PASSWORD);
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("password"), "password", "size", PASSWORD_TOO_SHORT_MESSAGE);
        }

        @Test
        void acceptsPasswordWithMinimumLength() {
            UserForm form = creationFormWithPassword(MIN_LENGTH_PASSWORD);
            BindingResult bindingResult = bindingResultFor(form);

            assertTrue(userService.validateBusinessRules(form, bindingResult, false));
        }

        @Test
        void rejectsStudentRole() {
            UserForm form = creationFormWithRole(UserRole.ALUNO);
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("role"), "role", "invalid", INVALID_ROLE_MESSAGE);
        }

        @Test
        void reportsEveryViolationInRuleOrder() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
            UserForm form = creationFormViolatingAllRules();
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));

            List<FieldError> errors = bindingResult.getFieldErrors();
            assertEquals(4, errors.size());
            assertFieldError(errors.get(0), "email", "duplicate", DUPLICATE_EMAIL_MESSAGE);
            assertFieldError(errors.get(1), "username", "duplicate", DUPLICATE_USERNAME_MESSAGE);
            assertFieldError(errors.get(2), "password", "size", PASSWORD_TOO_SHORT_MESSAGE);
            assertFieldError(errors.get(3), "role", "invalid", INVALID_ROLE_MESSAGE);
        }

        @Test
        void returnsFalseWhenBindingResultAlreadyHasErrors() {
            UserForm form = creationForm();
            BindingResult bindingResult = bindingResultFor(form);
            bindingResult.rejectValue("fullName", "NotBlank", "O nome completo é obrigatório.");

            assertFalse(userService.validateBusinessRules(form, bindingResult, false));
            assertEquals(1, bindingResult.getErrorCount());
        }
    }

    @Nested
    class ValidateBusinessRulesOnEdition {

        @Test
        void acceptsValidFormWithoutPassword() {
            UserForm form = editionForm();
            BindingResult bindingResult = bindingResultFor(form);

            assertTrue(userService.validateBusinessRules(form, bindingResult, true));
            verify(userRepository).existsByEmailAndIdNot(EMAIL, EXISTING_USER_ID);
            verify(userRepository).existsByUsernameAndIdNot(USERNAME, EXISTING_USER_ID);
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).existsByUsername(anyString());
        }

        @Test
        void acceptsBlankPassword() {
            UserForm form = editionFormWithPassword("   ");
            BindingResult bindingResult = bindingResultFor(form);

            assertTrue(userService.validateBusinessRules(form, bindingResult, true));
        }

        @Test
        void rejectsEmailUsedByAnotherUser() {
            when(userRepository.existsByEmailAndIdNot(EMAIL, EXISTING_USER_ID)).thenReturn(true);
            UserForm form = editionForm();
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, true));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("email"), "email", "duplicate", DUPLICATE_EMAIL_MESSAGE);
        }

        @Test
        void rejectsUsernameUsedByAnotherUser() {
            when(userRepository.existsByUsernameAndIdNot(USERNAME, EXISTING_USER_ID)).thenReturn(true);
            UserForm form = editionForm();
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, true));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("username"), "username", "duplicate", DUPLICATE_USERNAME_MESSAGE);
        }

        @Test
        void rejectsShortPassword() {
            UserForm form = editionFormWithPassword(SHORT_PASSWORD);
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, true));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("password"), "password", "size", PASSWORD_TOO_SHORT_MESSAGE);
        }

        @Test
        void rejectsStudentRole() {
            UserForm form = editionFormWithRole(UserRole.ALUNO);
            BindingResult bindingResult = bindingResultFor(form);

            assertFalse(userService.validateBusinessRules(form, bindingResult, true));
            assertEquals(1, bindingResult.getErrorCount());
            assertFieldError(bindingResult.getFieldError("role"), "role", "invalid", INVALID_ROLE_MESSAGE);
        }

        @Test
        void usesCreationQueriesWhenFormHasNoId() {
            UserForm form = editionFormWithoutId();
            BindingResult bindingResult = bindingResultFor(form);

            assertTrue(userService.validateBusinessRules(form, bindingResult, true));
            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository).existsByUsername(USERNAME);
        }
    }

    @Nested
    class CreateFromApi {

        @Test
        void savesValidUser() {
            PublicUserResponse response = userService.createFromApi(creationForm());

            assertAll(
                    () -> assertEquals(EMAIL, response.email()),
                    () -> assertEquals(USERNAME, response.username()),
                    () -> assertEquals(UserRole.PROFESSOR.name(), response.role()),
                    () -> assertTrue(response.enabled())
            );
            verify(passwordEncoder).encode(VALID_PASSWORD);
            verify(userRepository).save(any(User.class));
        }

        @Test
        void throwsConflictForDuplicateEmailAndStopsAtFirstViolation() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            UserForm form = creationFormViolatingAllRules();

            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.createFromApi(form)
            );

            assertEquals(DUPLICATE_EMAIL_MESSAGE, exception.getMessage());
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void throwsConflictForDuplicateUsername() {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
            UserForm form = creationForm();

            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.createFromApi(form)
            );

            assertEquals(DUPLICATE_USERNAME_MESSAGE, exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void throwsBadRequestForMissingPassword(String password) {
            UserForm form = creationFormWithPassword(password);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createFromApi(form)
            );

            assertEquals(PASSWORD_REQUIRED_MESSAGE, exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void throwsBadRequestForShortPassword() {
            UserForm form = creationFormWithPassword(SHORT_PASSWORD);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createFromApi(form)
            );

            assertEquals(PASSWORD_TOO_SHORT_MESSAGE, exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void throwsBadRequestForStudentRole() {
            UserForm form = creationFormWithRole(UserRole.ALUNO);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createFromApi(form)
            );

            assertEquals(INVALID_ROLE_MESSAGE, exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    class UpdateFromApi {

        @Test
        void updatesUserKeepingPasswordWhenBlank() {
            when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(existingUser()));

            PublicUserResponse response = userService.updateFromApi(editionForm());

            assertAll(
                    () -> assertEquals(EXISTING_USER_ID, response.id()),
                    () -> assertEquals(EMAIL, response.email()),
                    () -> assertEquals(USERNAME, response.username()),
                    () -> assertEquals(UserRole.PROFESSOR.name(), response.role())
            );
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        void updatesPasswordWhenInformed() {
            when(userRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(existingUser()));

            userService.updateFromApi(editionFormWithPassword(VALID_PASSWORD));

            verify(passwordEncoder).encode(VALID_PASSWORD);
        }

        @Test
        void throwsConflictForEmailUsedByAnotherUser() {
            when(userRepository.existsByEmailAndIdNot(EMAIL, EXISTING_USER_ID)).thenReturn(true);
            UserForm form = editionForm();

            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.updateFromApi(form)
            );

            assertEquals(DUPLICATE_EMAIL_MESSAGE, exception.getMessage());
            verify(userRepository, never()).existsByUsernameAndIdNot(anyString(), anyLong());
            verify(userRepository, never()).findById(anyLong());
        }

        @Test
        void throwsConflictForUsernameUsedByAnotherUser() {
            when(userRepository.existsByUsernameAndIdNot(USERNAME, EXISTING_USER_ID)).thenReturn(true);
            UserForm form = editionForm();

            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.updateFromApi(form)
            );

            assertEquals(DUPLICATE_USERNAME_MESSAGE, exception.getMessage());
            verify(userRepository, never()).findById(anyLong());
        }

        @Test
        void throwsBadRequestForShortPassword() {
            UserForm form = editionFormWithPassword(SHORT_PASSWORD);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateFromApi(form)
            );

            assertEquals(PASSWORD_TOO_SHORT_MESSAGE, exception.getMessage());
            verify(userRepository, never()).findById(anyLong());
        }

        @Test
        void throwsBadRequestForStudentRole() {
            UserForm form = editionFormWithRole(UserRole.ALUNO);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateFromApi(form)
            );

            assertEquals(INVALID_ROLE_MESSAGE, exception.getMessage());
            verify(userRepository, never()).findById(anyLong());
        }
    }
}
