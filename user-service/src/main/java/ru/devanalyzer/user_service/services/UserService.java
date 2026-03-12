package ru.devanalyzer.user_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.exceptions.UserNotFoundException;
import ru.devanalyzer.user_service.models.User;
import ru.devanalyzer.user_service.repositories.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository repository;

    @Autowired
    public UserService(BCryptPasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = repository.findAll().stream()
                .map(this::formatToResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    public UserResponse findById(Long userId) {
        User user = getUserOrThrow(userId);
        return formatToResponse(user);
    }

    public ResponseEntity<UserResponse> save(UserCreateRequest request) {
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
        return ResponseEntity.ok(formatToResponse(saved));
    }

    public ResponseEntity<UserResponse> updateUser(UserUpdateRequest request, Long userId) {
        User user = getUserOrThrow(userId);
        user.setFirstName(request.firstName());
        if (request.patronymic() != null) user.setPatronymic(request.patronymic());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.company() != null) user.setCompany(request.company());
        if (request.position() != null) user.setPosition(request.position());
        user.setUpdatedAt(OffsetDateTime.now());
        User saved = repository.save(user);
        return ResponseEntity.ok(formatToResponse(saved));
    }

    public ResponseEntity<Void> delete(Long id) {
            getUserOrThrow(id);
            repository.deleteById(id);
            return ResponseEntity.noContent().build();

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
