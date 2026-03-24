package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.director.DirectorService;

import java.util.Collection;

/**
 * Контроллер для работы с CRUD-операциями режиссеров фильмов.
 * Обрабатывает запросы к эндпоинтам /directors.
 */
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<Collection<Director>> getAllDirectors() {
        return ResponseEntity.ok(directorService.getAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> getDirectorById(
            @PathVariable @Positive(message = "ID режиссёра должен быть положительным") Long id) {
        return ResponseEntity.ok(directorService.getDirectorById(id));
    }

    @PostMapping
    public ResponseEntity<Director> createDirector(@Valid @RequestBody Director director) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(directorService.createDirector(director));
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody Director director) {
        return ResponseEntity.ok(directorService.updateDirector(director));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable Long id) {
        directorService.deleteDirector(id);
        return ResponseEntity.noContent().build();
    }
}