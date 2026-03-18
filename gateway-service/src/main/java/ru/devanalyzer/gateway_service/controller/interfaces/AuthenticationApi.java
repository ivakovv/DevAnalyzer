package ru.devanalyzer.gateway_service.controller.interfaces;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import ru.devanalyzer.gateway_service.dto.auth.LoginRequestDto;

public interface AuthenticationApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    ResponseEntity<Void> login(LoginRequestDto request, HttpServletResponse response);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Невалидный refresh токен"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный выход"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    ResponseEntity<Void> logout(HttpServletResponse response);

}
