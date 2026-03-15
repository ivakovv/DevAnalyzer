package ru.devanalyzer.user_service.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.user_service.security.UserPrincipal;
import ru.devanalyzer.user_service.services.StorageService;

import java.net.URL;

@Tag(name = "Avatars", description = "API для управления аватарками пользователей")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/avatars")
public class AvatarsController {

    private final StorageService storageService;

    @SecurityRequirement(name = "Gateway Authentication")
    @GetMapping()
    public URL getAvatar(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return storageService.generateViewablePresignedUrl("users/avatars/" + principal.getUserId().toString());
    }

    @SecurityRequirement(name = "Gateway Authentication")
    @PostMapping()
    public URL uploadAvatar(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return storageService.generateUploadablePresignedUrl("users/avatars/" + principal.getUserId().toString());
    }

}
