package ru.devanalyzer.user_service.controllers.interfaces;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.devanalyzer.user_service.security.UserPrincipal;

import java.net.URL;

@Tag(name = "Avatars", description = "API для управления аватарками пользователей")
public interface AvatarsApi {

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ссылка успешно получена"),
            @ApiResponse(responseCode = "404", description = "Такого объекта нет в хранилище"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    URL getAvatar(@Parameter(hidden = true) UserPrincipal principal) ;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ссылка успешно получена"),
            @ApiResponse(responseCode = "500", description = "Сервер в данный момент не доступен")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    URL uploadAvatar(@Parameter(hidden = true) UserPrincipal principal);
}
