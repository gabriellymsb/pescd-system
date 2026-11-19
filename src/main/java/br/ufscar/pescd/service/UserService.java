package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.PublicUserResponse;
import br.ufscar.pescd.dto.UserForm;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.exception.BusinessRuleException;
import br.ufscar.pescd.exception.DuplicateResourceException;
import br.ufscar.pescd.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public List<PublicUserResponse> findAllPublic() {
        return findAll().stream()
                .map(this::toPublicResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public PublicUserResponse findPublicById(Long id) {
        return toPublicResponse(findById(id));
    }

    public UserForm toForm(User user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setUsername(user.getUsername());
        form.setRole(user.getRole());
        return form;
    }

    public boolean validateBusinessRules(
            UserForm form,
            BindingResult bindingResult,
            boolean editing
    ) {
        findBusinessRuleViolations(form, editing).forEach(violation ->
                bindingResult.rejectValue(violation.field, violation.code, violation.message)
        );

        return !bindingResult.hasErrors();
    }

    @Transactional
    public void create(UserForm form) {
        User user = new User();
        applyForm(user, form, true);
        userRepository.save(user);
    }

    @Transactional
    public PublicUserResponse createFromApi(UserForm form) {
        validateBusinessRulesOrThrow(form, false);
        User user = new User();
        applyForm(user, form, true);
        return toPublicResponse(userRepository.save(user));
    }

    @Transactional
    public void update(UserForm form) {
        User user = findById(form.getId());
        applyForm(user, form, false);
        userRepository.save(user);
    }

    @Transactional
    public PublicUserResponse updateFromApi(UserForm form) {
        validateBusinessRulesOrThrow(form, true);
        User user = findById(form.getId());
        applyForm(user, form, false);
        return toPublicResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id, String loggedUsername) {
        User user = findById(id);

        if (user.getUsername().equals(loggedUsername)) {
            throw new IllegalArgumentException("Você não pode remover seu próprio usuário.");
        }

        userRepository.delete(user);
    }

    @Transactional
    public PublicUserResponse block(Long id, String loggedUsername) {
        return changeEnabled(id, loggedUsername, false);
    }

    @Transactional
    public PublicUserResponse unblock(Long id, String loggedUsername) {
        return changeEnabled(id, loggedUsername, true);
    }

    private PublicUserResponse changeEnabled(Long id, String loggedUsername, boolean enabled) {
        User user = findById(id);

        if (user.getUsername().equals(loggedUsername)) {
            throw new BusinessRuleException("Você não pode bloquear ou desbloquear seu próprio usuário.");
        }

        user.setEnabled(enabled);
        return toPublicResponse(userRepository.save(user));
    }

    private void applyForm(User user, UserForm form, boolean creating) {
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setUsername(form.getUsername());
        user.setRole(form.getRole());

        if (creating) {
            user.setEnabled(true);
        }

        if (creating || (form.getPassword() != null && !form.getPassword().isBlank())) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
    }

    private PublicUserResponse toPublicResponse(User user) {
        return new PublicUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                Boolean.TRUE.equals(user.getEnabled())
        );
    }

    private void validateBusinessRulesOrThrow(UserForm form, boolean editing) {
        findBusinessRuleViolations(form, editing).findFirst().ifPresent(violation -> {
            throw violation.toException();
        });
    }

    private Stream<UserRuleViolation> findBusinessRuleViolations(UserForm form, boolean editing) {
        return Arrays.stream(UserRuleViolation.values())
                .filter(violation -> isViolated(violation, form, editing));
    }

    private boolean isViolated(UserRuleViolation violation, UserForm form, boolean editing) {
        Long currentId = editing ? form.getId() : null;

        return switch (violation) {
            case DUPLICATE_EMAIL -> currentId == null
                    ? userRepository.existsByEmail(form.getEmail())
                    : userRepository.existsByEmailAndIdNot(form.getEmail(), currentId);
            case DUPLICATE_USERNAME -> currentId == null
                    ? userRepository.existsByUsername(form.getUsername())
                    : userRepository.existsByUsernameAndIdNot(form.getUsername(), currentId);
            case PASSWORD_REQUIRED -> !editing && !hasText(form.getPassword());
            case PASSWORD_TOO_SHORT -> hasText(form.getPassword())
                    && form.getPassword().length() < MIN_PASSWORD_LENGTH;
            case INVALID_ROLE -> form.getRole() == UserRole.ALUNO;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private enum UserRuleViolation {
        DUPLICATE_EMAIL("email", "duplicate", "Já existe um usuário com este e-mail.", true),
        DUPLICATE_USERNAME("username", "duplicate", "Já existe um usuário com este nome de usuário.", true),
        PASSWORD_REQUIRED("password", "required", "A senha é obrigatória.", false),
        PASSWORD_TOO_SHORT("password", "size", "A senha deve ter pelo menos 6 caracteres.", false),
        INVALID_ROLE("role", "invalid", "O perfil deve ser Administrador, Secretário ou Professor.", false);

        private final String field;
        private final String code;
        private final String message;
        private final boolean conflict;

        UserRuleViolation(String field, String code, String message, boolean conflict) {
            this.field = field;
            this.code = code;
            this.message = message;
            this.conflict = conflict;
        }

        private RuntimeException toException() {
            return conflict
                    ? new DuplicateResourceException(message)
                    : new IllegalArgumentException(message);
        }
    }
}
