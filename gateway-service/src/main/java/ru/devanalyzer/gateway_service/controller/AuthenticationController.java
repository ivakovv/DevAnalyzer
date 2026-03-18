package ru.devanalyzer.gateway_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.gateway_service.controller.interfaces.AuthenticationApi;
import ru.devanalyzer.gateway_service.dto.auth.LoginRequestDto;
import ru.devanalyzer.gateway_service.service.auth.AuthenticationService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController implements AuthenticationApi {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        authenticationService.login(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.refresh(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authenticationService.logout(response);
        return ResponseEntity.ok().build();
    }
}