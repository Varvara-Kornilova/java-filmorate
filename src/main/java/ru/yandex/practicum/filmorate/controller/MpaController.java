package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;

import java.util.Collection;

/**
 * Контроллер для работы с возрастными рейтингами (MPA).
 * Обрабатывает запросы к эндпоинтам /mpa.
 */
@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    /**
     * Возвращает все доступные возрастные рейтинги.
     */
    @GetMapping
    public Collection<Mpa> listAllRatings() {
        log.info("Запрошен список всех возрастных рейтингов MPA");
        return mpaService.getAllRatings();
    }

    /**
     * Находит и возвращает рейтинг по идентификатору.
     * @param id идентификатор рейтинга
     */
    @GetMapping("/{id}")
    public Mpa fetchRatingById(@PathVariable @Positive(message = "ID рейтинга должен быть больше нуля") Long id) {
        log.trace("Получение рейтинга MPA с id={}", id);
        return mpaService.getRatingById(id);
    }
}
