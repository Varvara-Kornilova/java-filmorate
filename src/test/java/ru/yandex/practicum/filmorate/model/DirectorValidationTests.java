package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DirectorValidationTests {

    @Autowired
    private Validator validator;

    private Director createValidDirector() {
        Director director = new Director();
        director.setName("Квентин Тарантино");
        return director;
    }

    @Test
    public void shouldHaveNoViolationsForValidDirector() {
        Director director = createValidDirector();
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldRejectNullName() {
        Director director = createValidDirector();
        director.setName(null);
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertEquals(1, violations.size());
        assertEquals("Имя режиссёра не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldRejectEmptyName() {
        Director director = createValidDirector();
        director.setName("");
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertEquals(1, violations.size());
        assertEquals("Имя режиссёра не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldRejectBlankName() {
        Director director = createValidDirector();
        director.setName("   ");
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertEquals(1, violations.size());
        assertEquals("Имя режиссёра не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldAcceptDirectorWithLongName() {
        Director director = createValidDirector();
        director.setName("Очень длинное имя режиссёра, которое может содержать много символов и всё равно быть валидным");
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldAcceptDirectorWithSpecialCharactersInName() {
        Director director = createValidDirector();
        director.setName("Jean-Luc Godard");
        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldIgnoreIdFieldInValidation() {
        Director director = new Director();
        director.setName("Новый режиссёр");

        Set<ConstraintViolation<Director>> violations = validator.validate(director);
        assertTrue(violations.isEmpty(), "Поле id не должно валидироваться при создании");
    }
}