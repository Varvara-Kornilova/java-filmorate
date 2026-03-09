package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
@Transactional
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       MpaService mpaService,
                       GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
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
        return filmStorage.update(updatedFilm);
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Фильм с идентификатором %d не найден", filmId)));
    }

    public Collection<Film> getMostPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10; // Значение по умолчанию
        }
        return filmStorage.getPopular(count);
    }

    public void likeFilm(Long filmId, Long userId) {
        validateFilmAndUser(filmId, userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        validateFilmAndUser(filmId, userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
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
                            .map(g -> g.getId())
                            .collect(java.util.stream.Collectors.toSet()));
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
}
