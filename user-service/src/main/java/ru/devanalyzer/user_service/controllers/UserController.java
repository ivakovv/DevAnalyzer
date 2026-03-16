package ru.devanalyzer.user_service.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.devanalyzer.user_service.dto.PasswordChangeRequest;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.security.UserPrincipal;
import ru.devanalyzer.user_service.services.UserService;

import java.util.List;

@Tag(name = "User Management", description = "API для управления пользователями")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }


    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Данные пользователя успешно получены"),
        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> findById(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.findById(principal.getUserId()));
    }


    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
        @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> saveUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.save(request));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Данные успешно обновлены"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные"),
        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> changeUserData(
            @Valid @RequestBody UserUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.updateUser(request, principal.getUserId()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeRequest request,
    @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(request.password(),principal.getUserId());
        return ResponseEntity.ok().build();
    }
}
