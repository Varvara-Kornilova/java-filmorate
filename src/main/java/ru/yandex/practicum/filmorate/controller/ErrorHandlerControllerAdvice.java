package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationErrorResponse;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandlerControllerAdvice {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Violation handleValidationException(final ValidationException e) {
        return new Violation("validation", e.getMessage());
    }

    @ExceptionHandler(DuplicateDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Violation handleDuplicateData(final DuplicateDataException e) {
        return new Violation("duplicate", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Violation handleNotFoundException(final NotFoundException e) {
        return new Violation("not_found", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e
    ) {
        final List<Violation> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .collect(Collectors.toList());
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        final List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Violation handleError(final Exception e, HttpServletRequest request) {
        // Не обрабатывать ошибки H2 Console — пусть H2 сам вернёт HTML
        if (request != null && request.getRequestURI().startsWith("/h2-console")) {
            // Оборачиваем в RuntimeException, чтобы Spring не пытался обработать дальше
            throw new RuntimeException(e);
        }

        return new Violation("internal_error", "Произошла непредвиденная ошибка.");
    }
}
