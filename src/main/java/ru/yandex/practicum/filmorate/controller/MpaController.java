package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;

import java.util.Collection;

/**
 * Контроллер для работы с возрастными рейтингами (MPA).
 * Обрабатывает запросы к эндпоинтам /mpa.
 */
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public Collection<Mpa> listAllRatings() {
        return mpaService.getAllRatings();
    }

    @GetMapping("/{id}")
    public Mpa fetchRatingById(@PathVariable @Positive(message = "ID рейтинга должен быть больше нуля") Long id) {
        return mpaService.getRatingById(id);
    }
}