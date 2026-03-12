package ru.devanalyzer.user_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.repositories.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<List<UserResponse>> findAll() {
    }

    public UserResponse findById(Long userId) {
    }

    public ResponseEntity<UserResponse> save(UserCreateRequest request) {
    }

    public ResponseEntity<UserResponse> updateUser(UserUpdateRequest request, Long userId) {
    }

    public ResponseEntity<Void> delete(Long id) {
    }
}
