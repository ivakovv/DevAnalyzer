package ru.devanalyzer.user_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.user_service.controllers.interfaces.AvatarsApi;
import ru.devanalyzer.user_service.security.UserPrincipal;
import ru.devanalyzer.user_service.services.StorageService;

import java.net.URL;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/avatars")
public class AvatarsController implements AvatarsApi {

    private final StorageService storageService;

    @GetMapping()
    public URL getAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        return storageService.generateViewablePresignedUrl("users/avatars/" + principal.getUserId().toString());
    }

    @PostMapping()
    public URL uploadAvatar(@AuthenticationPrincipal UserPrincipal principal) {
        return storageService.generateUploadablePresignedUrl("users/avatars/" + principal.getUserId().toString());
    }
}
