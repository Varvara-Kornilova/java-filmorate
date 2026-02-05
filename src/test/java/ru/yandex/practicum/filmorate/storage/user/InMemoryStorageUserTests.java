package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryStorageUserTests {

    private InMemoryUserStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName(null);
        storage.useLoginAsNameIfNameIsNotValid(user);

        assertEquals("testlogin", user.getName());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsEmpty() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName("");
        storage.useLoginAsNameIfNameIsNotValid(user);

        assertEquals("testlogin", user.getName());
    }

    @Test
    void shouldNotChangeNameWhenItIsValid() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName("Real Name");
        storage.useLoginAsNameIfNameIsNotValid(user);

        assertEquals("Real Name", user.getName());
    }
}