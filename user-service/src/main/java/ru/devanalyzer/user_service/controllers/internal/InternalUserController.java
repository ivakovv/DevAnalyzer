package ru.devanalyzer.user_service.controllers.internal;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
