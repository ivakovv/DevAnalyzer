package ru.devanalyzer.user_service.controllers.internal;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.user_service.dto.auth.UserValidationResponse;
import ru.devanalyzer.user_service.services.UserService;

@Hidden
@Tag(name = "Internal API", description = "Внутренние endpoints для межсервисного взаимодействия")
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Пользователь успешно валидирован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Неверный пароль")
    })
    @PostMapping("/validate")
    public ResponseEntity<UserValidationResponse> validateUser(
            @Parameter(description = "Email пользователя", required = true)
            @RequestHeader("X-Auth-Email") String email,
            @Parameter(description = "Пароль пользователя", required = true)
            @RequestHeader("X-Auth-Password") String password) {
        return ResponseEntity.ok(userService.validateUser(email, password));
    }
}
