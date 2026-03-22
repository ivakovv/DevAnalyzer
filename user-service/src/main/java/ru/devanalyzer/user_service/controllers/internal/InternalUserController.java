package ru.devanalyzer.user_service.controllers.internal;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.user_service.dto.ResetPasswordRequest;
import ru.devanalyzer.user_service.dto.auth.UserValidationResponse;
import ru.devanalyzer.user_service.services.UserService;

@Hidden
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping("/validate")
    public ResponseEntity<UserValidationResponse> validateUser(
            @Parameter(description = "Email пользователя", required = true)
            @RequestHeader("X-Auth-Email") String email,
            @Parameter(description = "Пароль пользователя", required = true)
            @RequestHeader("X-Auth-Password") String password) {
        return ResponseEntity.ok(userService.validateUser(email, password));
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserValidationResponse> findByEmail(
            @Parameter(description = "Email пользователя", required = true)
            @RequestParam String email) {
        UserValidationResponse response = userService.findByEmail(email);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Новый пароль", required = true)
            @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(userId, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
