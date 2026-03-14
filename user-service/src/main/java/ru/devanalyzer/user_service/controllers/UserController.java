package ru.devanalyzer.user_service.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.services.UserService;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(service.findAll());
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> findById(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.findById(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> saveUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(service.save(request));
    }


    @PutMapping("/me")
    public ResponseEntity<UserResponse> changeUserData(@Valid @RequestBody UserUpdateRequest request,
                                       @RequestHeader("X-User-Id") Long userId,
                                       @RequestHeader("X-User-Email") String email,
                                       @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(service.updateUser(request,userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
