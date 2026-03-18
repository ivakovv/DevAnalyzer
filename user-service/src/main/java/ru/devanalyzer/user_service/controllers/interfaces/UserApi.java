package ru.devanalyzer.user_service.controllers.interfaces;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import ru.devanalyzer.user_service.dto.PasswordChangeRequest;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.security.UserPrincipal;

import java.util.List;

@Tag(name = "User Management", description = "API для управления пользователями")
public interface UserApi {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<List<UserResponse>> getAllUsers();

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные пользователя успешно получены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<UserResponse> findById(@Parameter(hidden = true) UserPrincipal principal);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    ResponseEntity<UserResponse> saveUser(UserCreateRequest request);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<UserResponse> changeUserData(UserUpdateRequest request, @Parameter(hidden = true) UserPrincipal principal);

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<Void> deleteUser(Long id);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<Void> changePassword(PasswordChangeRequest request, @Parameter(hidden = true) UserPrincipal principal);
}
