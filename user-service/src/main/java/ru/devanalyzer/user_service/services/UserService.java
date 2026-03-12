package ru.devanalyzer.user_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.exceptions.UserAlreadyExistsException;
import ru.devanalyzer.user_service.exceptions.UserNotFoundException;
import ru.devanalyzer.user_service.models.User;
import ru.devanalyzer.user_service.repositories.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;


    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.debug("Processing getAllUsers request");
        return repository.findAll().stream()
                .map(this::formatToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        log.debug("Processing findById request with userId={}", userId);
        User user = getUserOrThrow(userId);
        return formatToResponse(user);
    }

    @Transactional
    public UserResponse save(UserCreateRequest request) {
                if (repository.existsByEmail(request.email())) {
                    log.warn("Attempt to register with existing email");
                    throw new UserAlreadyExistsException("User with this email already exists:" + request.email());
                }

                User saved = repository.save(User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .patronymic(request.patronymic())
                .lastName(request.lastName())
                .role(request.role())
                .company(request.company())
                .position(request.position())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        log.info("User registered successfully, userId={}", saved.getId());
        return formatToResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(UserUpdateRequest request, Long userId) {
        log.debug("Processing update request");
        User user = getUserOrThrow(userId);
        if(request.firstName() != null) user.setFirstName(request.firstName());
        if (request.patronymic() != null) user.setPatronymic(request.patronymic());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.company() != null) user.setCompany(request.company());
        if (request.position() != null) user.setPosition(request.position());
        user.setUpdatedAt(OffsetDateTime.now());
        User saved = repository.save(user);
        log.info("User updated successfully");
        return formatToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        getUserOrThrow(id);
            repository.deleteById(id);
        log.info("Deleted user, userId={}", id);
    }
    private UserResponse formatToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .patronymic(user.getPatronymic())
                .lastName(user.getLastName())
                .role(user.getRole())
                .company(user.getCompany())
                .position(user.getPosition())
                .build();
    }

    private User getUserOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with this id was not found: " + id));
    }
}
