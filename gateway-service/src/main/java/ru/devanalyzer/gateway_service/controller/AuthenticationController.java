package ru.devanalyzer.gateway_service.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.devanalyzer.gateway_service.dto.auth.LoginRequestDto;
import ru.devanalyzer.gateway_service.service.auth.AuthenticationService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        authenticationService.login(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Невалидный refresh токен"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.refresh(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный выход"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authenticationService.logout(response);
        return ResponseEntity.ok().build();
    }
}