package ru.devanalyzer.user_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.devanalyzer.user_service.dto.UserCreateRequest;
import ru.devanalyzer.user_service.dto.UserResponse;
import ru.devanalyzer.user_service.dto.UserUpdateRequest;
import ru.devanalyzer.user_service.enums.Role;
import ru.devanalyzer.user_service.exceptions.UserAlreadyExistsException;
import ru.devanalyzer.user_service.exceptions.UserNotFoundException;
import ru.devanalyzer.user_service.models.User;
import ru.devanalyzer.user_service.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private User user;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private User updatedUser;

    @BeforeEach
    void init() {
        user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .firstName("Max")
                .lastName("Ivanov")
                .role(Role.USER)
                .build();
        this.createRequest = new UserCreateRequest(
                "test@mail.com", "password123", "Igor", "Petrovich", "Igorev",null, null);
        this.updateRequest = new UserUpdateRequest("Maxim","Olegovich","Ivakov","Tbank","Cleaner");
         updatedUser = User.builder()
                .id(1L)
                .email("test@mail.com")
                .firstName(updateRequest.firstName())
                .lastName(updateRequest.lastName())
                .role(Role.USER)
                .build();
    }

    @Test
    void shouldReturnAllUsers() {
        when(repository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals("test@mail.com", result.get(0).email());
        assertEquals("Max", result.get(0).firstName());
}

    @Test
    void shouldFindUserWithExistingId() {

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.findById(1L);

        assertEquals(user.getEmail(),result.email());
        assertEquals(user.getFirstName(),result.firstName());
        assertEquals(user.getPatronymic(),result.patronymic());
        assertEquals(user.getLastName(),result.lastName());
    }


    @Test
    void shouldNotFindUserWithNonExistingId(){
        when(repository.findById(13L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,() -> userService.findById(13L));
    }


    @Test
    void shouldNotSaveUserWithThisEmail() {
        when(repository.existsByEmail(createRequest.email())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.save(createRequest));
    }

    @Test
    void shouldSaveUserWithThisEmail() {
        when(repository.existsByEmail(createRequest.email())).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("password");

        UserResponse result = userService.save(createRequest);
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getFirstName(), result.firstName());
    }

    @Test
    void shouldNotUpdateUser() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,() -> userService.updateUser(updateRequest,1L));
    }


    @Test
    void shouldUpdateUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(updateRequest,1L);

        assertEquals(updatedUser.getFirstName(),result.firstName());
        assertEquals(updatedUser.getLastName(),result.lastName());

        verify(repository).save(argThat(savedUser ->
                savedUser.getFirstName().equals(updateRequest.firstName()) &&
                        savedUser.getLastName().equals(updateRequest.lastName()) &&
                        savedUser.getPatronymic().equals(updateRequest.patronymic()) &&
                        savedUser.getCompany().equals(updateRequest.company()) &&
                        savedUser.getPosition().equals(updateRequest.position())
        ));
    }


    @Test
    void shouldDeleteUserWithExistingId(){
        when(repository.findById(13L)).thenReturn(Optional.of(user));
        userService.deleteUser(13L);
        verify(repository).deleteById(13L);
    }


}