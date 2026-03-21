package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final DirectorStorage directorStorage;
    private final GenreStorage genreStorage;
    private final EventService eventService;

    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       MpaService mpaService,
                       GenreService genreService,
                       DirectorService directorService,
                       DirectorStorage directorStorage,
                       GenreStorage genreStorage,
                       EventService eventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
        this.directorStorage = directorStorage;
        this.genreStorage = genreStorage;
        this.eventService = eventService;
    }

    public Collection<Film> getAllFilms() {
        Collection<Film> films = filmStorage.findAll();
        films.forEach(this::sortFilmCollections);
        return films;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);

        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = extractGenreIds(film.getGenres());
            genreStorage.setGenres(createdFilm.getId(), genreIds);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> directorIds = extractDirectorIds(film.getDirectors());
            directorStorage.setDirectors(createdFilm.getId(), directorIds);
        }

        return getFilmById(createdFilm.getId());
    }

    public Film editFilm(Film updatedFilm) {
        if (updatedFilm.getId() == null) {
            log.warn("Попытка обновления фильма без ID");
            throw new ValidationException("Идентификатор фильма должен быть указан");
        }

        if (!filmStorage.contains(updatedFilm.getId())) {
            throw new NotFoundException(
                    String.format("Фильм с идентификатором %d не найден", updatedFilm.getId()));
        }

        validateFilm(updatedFilm);
        filmStorage.update(updatedFilm);

        // Очищаем старые жанры фильма
        genreStorage.updateFilmGenres(updatedFilm.getId());

        // Сохраняем новые жанры фильма
        if (updatedFilm.getGenres() != null && !updatedFilm.getGenres().isEmpty()) {
            Set<Long> genreIds = extractGenreIds(updatedFilm.getGenres());
            genreStorage.setGenres(updatedFilm.getId(), genreIds);
        }

        // Очищаем старых режиссёров фильма
        directorStorage.updateFilmDirectors(updatedFilm.getId());

        // Сохраняем новых режиссёров фильма
        if (updatedFilm.getDirectors() != null && !updatedFilm.getDirectors().isEmpty()) {
            Set<Long> directorIds = extractDirectorIds(updatedFilm.getDirectors());
            directorStorage.setDirectors(updatedFilm.getId(), directorIds);
        }

        return getFilmById(updatedFilm.getId());
    }

    public Film getFilmById(Long filmId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Фильм с идентификатором %d не найден", filmId)));
        sortFilmCollections(film);
        return film;
    }

    // Получить популярные фильмы с фильтрацией по жанру и году
    public Collection<Film> getMostPopularFilms(Integer count, Long genreId, Integer year) {
        if (count == null || count <= 0) {
            count = 10;
        }

        if (genreId != null) {
            genreService.getGenreById(genreId);
        }

        Collection<Film> films = filmStorage.getPopular(count, genreId, year);
        films.forEach(this::sortFilmCollections);
        return films;
    }

    public void likeFilm(Long filmId, Long userId) {
        validateFilmAndUser(filmId, userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);

        // записываем событие добавления лайка
        eventService.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        validateFilmAndUser(filmId, userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);

        // записываем событие удаления лайка
        eventService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }

    public void deleteFilm(Long filmId) {
        if (!filmStorage.contains(filmId)) {
            throw new NotFoundException(
                    String.format("Фильм с идентификатором %d не найден", filmId));
        }
        filmStorage.delete(filmId);
        log.info("Фильм с идентификатором {} удалён", filmId);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException(
                    "Максимальная длина описания фильма — 200 символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException(
                    String.format("Дата релиза не может быть раньше %s", MIN_RELEASE_DATE));
        }

        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.getRatingById(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreService.validateGenres(
                    film.getGenres().stream()
                            .map(Genre::getId)
                            .collect(Collectors.toSet()));
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorService.validateDirectors(
                    film.getDirectors().stream()
                            .map(Director::getId)
                            .collect(Collectors.toSet()));
        }
    }

    private void validateFilmAndUser(Long filmId, Long userId) {
        if (!filmStorage.contains(filmId)) {
            throw new NotFoundException(
                    String.format("Фильм с идентификатором %d не найден", filmId));
        }

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", userId));
        }
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.getDirectorById(directorId);
        String sort = (sortBy != null && sortBy.equalsIgnoreCase("likes")) ? "likes" : "year";
        Collection<Film> films = filmStorage.findByDirectorId(directorId, sort);
        films.forEach(this::sortFilmCollections);
        return films;
    }

    public Collection<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Collection<Film> films = filmStorage.search(query, by);
        films.forEach(this::sortFilmCollections);
        return films;
    }

    // Достаём id жанров из набора жанров
    private Set<Long> extractGenreIds(Set<Genre> genres) {
        return genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
    }

    // Достаём id режиссёров из набора режиссёров
    private Set<Long> extractDirectorIds(Set<Director> directors) {
        return directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
    }

    // Сортируем жанры и режиссёров у фильма по id
    private void sortFilmCollections(Film film) {
        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        if (film.getDirectors() != null) {
            film.setDirectors(film.getDirectors().stream()
                    .sorted(Comparator.comparing(Director::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }
}