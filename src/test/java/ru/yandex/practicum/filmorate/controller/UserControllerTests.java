package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserControllerTests {

    public UserController controller = new UserController();

    @Test
    public void shouldSetNameToLoginWhenNameIsNull() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName(null);
        controller.useLoginAsNameIfNameIsNotValid(user);

        assertEquals("testlogin", user.getName());
    }

    @Test
    public void shouldSetNameToLoginWhenNameIsEmpty() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName("");
        controller.useLoginAsNameIfNameIsNotValid(user);

        assertEquals("testlogin", user.getName());
    }

    @Test
    public void shouldNotChangeNameWhenItIsValid() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName("Real Name");
        controller.useLoginAsNameIfNameIsNotValid(user);

        assertEquals("Real Name", user.getName());
    }
}
