package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.controller.Violation;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {
    private final List<Violation> violations;
}
