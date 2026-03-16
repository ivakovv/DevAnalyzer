package ru.devanalyzer.user_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.enums.Role;
import ru.devanalyzer.user_service.exceptions.UserNotFoundException;
import ru.devanalyzer.user_service.services.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    private final UserResponse userResponse = UserResponse.builder()
            .id(1L)
            .email("test@mail.com")
            .firstName("Max")
            .lastName("Ivanov")
            .role(Role.USER)
            .build();

    @Test
    void shouldReturnAllUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@mail.com"))
                .andExpect(jsonPath("$[0].firstName").value("Max"));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        when(userService.findById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.firstName").value("Max"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenThrow(new UserNotFoundException("Not found"));

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "test@mail.com", "password123", "Max", null, "Ivanov",null, null
        );
        when(userService.save(any())).thenReturn(userResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }


    @Test
    void shouldUpdateUser() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Maxim", "Olegovich", "Ivakov", "Tbank", "Dev");
        when(userService.updateUser(any(), eq(1L))).thenReturn(userResponse);

        mockMvc.perform(put("/users/me")
                        .header("X-User-Id", 1L)
                        .header("X-User-Email", "test@mail.com")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404OnDeleteWhenNotFound() throws Exception {
        doThrow(new UserNotFoundException("Not found")).when(userService).delete(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}
