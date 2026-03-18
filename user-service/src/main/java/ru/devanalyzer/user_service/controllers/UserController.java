package ru.devanalyzer.user_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.user_service.controllers.interfaces.UserApi;
import ru.devanalyzer.user_service.dto.PasswordChangeRequest;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.security.UserPrincipal;
import ru.devanalyzer.user_service.services.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> findById(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.findById(principal.getUserId()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> saveUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.save(request));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> changeUserData(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.updateUser(request, principal.getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest request,
    @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(request.password(),principal.getUserId());
        return ResponseEntity.ok().build();
    }
}
