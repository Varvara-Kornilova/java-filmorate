package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTests {

    private final UserController userController = new UserController();

    @Test
    void shouldThrowOnNullEmail() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void shouldThrowOnBlankEmail() {
        User user = new User();
        user.setEmail("   ");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void shouldThrowOnEmailWithoutAt() {
        User user = new User();
        user.setEmail("invalid-email.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void shouldThrowOnNullLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldThrowOnBlankLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldThrowOnLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("log in");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldSetNameToLoginIfNameIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> userController.validateUser(user));
        assertEquals("testlogin", user.getName());
    }

    @Test
    void shouldSetNameToLoginIfNameIsEmpty() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> userController.validateUser(user));
        assertEquals("testlogin", user.getName());
    }

    @Test
    void shouldThrowOnFutureBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.validateUser(user));
        assertEquals("дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldAcceptValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> userController.validateUser(user));
    }
}
