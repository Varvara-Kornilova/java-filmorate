package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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

    public Collection<Film> getAllFilms() {
        log.debug("Запрошен список всех фильмов");
        Collection<Film> films = filmStorage.findAll();
        films.forEach(this::sortFilmCollections);
        log.debug("Найдено {} фильмов", films.size());
        return films;
    }

    public Film addFilm(Film film) {
        log.debug("Создание фильма: name='{}'", film.getName());
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);

        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = extractGenreIds(film.getGenres());
            genreStorage.setGenres(createdFilm.getId(), genreIds);
            log.debug("Фильму id={} добавлены жанры: {}", createdFilm.getId(), genreIds);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> directorIds = extractDirectorIds(film.getDirectors());
            directorStorage.setDirectors(createdFilm.getId(), directorIds);
            log.debug("Фильму id={} добавлены режиссёры: {}", createdFilm.getId(), directorIds);
        }

        Film result = getFilmById(createdFilm.getId());
        log.info("Фильм создан: id={}, name='{}'", result.getId(), result.getName());
        return result;
    }

    public Film editFilm(Film updatedFilm) {
        log.debug("Обновление фильма: id={}", updatedFilm.getId());

        if (updatedFilm.getId() == null) {
            log.warn("Попытка обновления фильма без ID");
            throw new ValidationException("Идентификатор фильма должен быть указан");
        }

        if (!filmStorage.contains(updatedFilm.getId())) {
            log.warn("Фильм с id={} не найден при обновлении", updatedFilm.getId());
            throw new NotFoundException(
                    String.format("Фильм с идентификатором %d не найден", updatedFilm.getId()));
        }

        validateFilm(updatedFilm);
        filmStorage.update(updatedFilm);

        genreStorage.updateFilmGenres(updatedFilm.getId());

        if (updatedFilm.getGenres() != null && !updatedFilm.getGenres().isEmpty()) {
            Set<Long> genreIds = extractGenreIds(updatedFilm.getGenres());
            genreStorage.setGenres(updatedFilm.getId(), genreIds);
            log.debug("У фильма с id={} обновлены жанры: {}", updatedFilm.getId(), genreIds);
        }

        directorStorage.updateFilmDirectors(updatedFilm.getId());

        if (updatedFilm.getDirectors() != null && !updatedFilm.getDirectors().isEmpty()) {
            Set<Long> directorIds = extractDirectorIds(updatedFilm.getDirectors());
            directorStorage.setDirectors(updatedFilm.getId(), directorIds);
            log.debug("У фильма с id={} обновлены режиссёры: {}", updatedFilm.getId(), directorIds);
        }

        Film result = getFilmById(updatedFilm.getId());
        log.info("Фильм обновлён: id={}, name='{}'", result.getId(), result.getName());
        return result;
    }

    public Film getFilmById(Long filmId) {
        log.debug("Поиск фильма по id={}", filmId);
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", filmId);
                    return new NotFoundException(
                            String.format("Фильм с идентификатором %d не найден", filmId));
                });
        sortFilmCollections(film);
        log.debug("Фильм найден: id={}, name='{}'", film.getId(), film.getName());
        return film;
    }

    public Collection<Film> getMostPopularFilms(Integer count, Long genreId, Integer year) {
        log.debug("Запрос популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);

        if (count == null || count <= 0) {
            count = 10;
            log.debug("count не задан или невалиден, установлено значение по умолчанию: 10");
        }

        if (genreId != null) {
            genreService.getGenreById(genreId);
            log.debug("Жанр с id={} валидирован", genreId);
        }

        Collection<Film> films = filmStorage.getPopular(count, genreId, year);
        films.forEach(this::sortFilmCollections);
        log.debug("Найдено {} популярных фильмов", films.size());
        return films;
    }

    public void likeFilm(Long filmId, Long userId) {
        log.debug("Постановка лайка: userId={}, filmId={}", userId, filmId);
        validateFilmAndUser(filmId, userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);

        eventService.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        log.debug("Удаление лайка: userId={}, filmId={}", userId, filmId);
        validateFilmAndUser(filmId, userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);

        eventService.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }

    public void deleteFilm(Long filmId) {
        log.debug("Удаление фильма с id={}", filmId);

        if (!filmStorage.contains(filmId)) {
            log.warn("Попытка удаления несуществующего фильма с id={}", filmId);
            throw new NotFoundException(
                    String.format("Фильм с идентификатором %d не найден", filmId));
        }

        filmStorage.delete(filmId);
        log.info("Фильм с идентификатором {} удалён", filmId);
    }

    private void validateFilm(Film film) {
        log.debug("Валидация фильма: name='{}'", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: пустое название фильма");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание фильма превышает 200 символов");
            throw new ValidationException(
                    "Максимальная длина описания фильма — 200 символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше минимальной {}",
                    film.getReleaseDate(), MIN_RELEASE_DATE);
            throw new ValidationException(
                    String.format("Дата релиза не может быть раньше %s", MIN_RELEASE_DATE));
        }

        if (film.getDuration() != null && film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма не положительная: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.getRatingById(film.getMpa().getId());
            log.debug("MPA-рейтинг id={} валидирован", film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            genreService.validateGenres(genreIds);
            log.debug("Жанры фильма валидированы: {}", genreIds);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> directorIds = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorService.validateDirectors(directorIds);
            log.debug("Режиссёры фильма валидированы: {}", directorIds);
        }

        log.info("Валидация фильма успешно завершена");
    }

    private void validateFilmAndUser(Long filmId, Long userId) {
        log.debug("Валидация существования фильма id={} и пользователя id={}", filmId, userId);

        if (!filmStorage.contains(filmId)) {
            log.warn("Фильм с id={} не найден при валидации", filmId);
            throw new NotFoundException(
                    String.format("Фильм с идентификатором %d не найден", filmId));
        }

        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Пользователь с id={} не найден при валидации", userId);
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", userId));
        }
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        log.debug("Запрос фильмов режиссёра id={}, sortBy={}", directorId, sortBy);
        directorService.getDirectorById(directorId);
        String sort = (sortBy != null && sortBy.equalsIgnoreCase("likes")) ? "likes" : "year";
        Collection<Film> films = filmStorage.findByDirectorId(directorId, sort);
        films.forEach(this::sortFilmCollections);
        log.debug("Найдено {} фильмов режиссёра {}", films.size(), directorId);
        return films;
    }

    public Collection<Film> searchFilms(String query, String by) {
        log.debug("Поиск фильмов: query='{}', by='{}'", query, by);

        if (query == null || query.isBlank()) {
            log.debug("Поисковый запрос пуст, возвращён пустой список");
            return List.of();
        }

        Collection<Film> films = filmStorage.search(query, by);
        films.forEach(this::sortFilmCollections);
        log.debug("Поиск вернул {} результатов", films.size());
        return films;
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        log.debug("Поиск общих фильмов: userId={}, friendId={}", userId, friendId);

        if (!userStorage.findById(userId).isPresent()) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", userId));
        }

        if (!userStorage.findById(friendId).isPresent()) {
            log.warn("Пользователь с id={} не найден", friendId);
            throw new NotFoundException(
                    String.format("Пользователь с идентификатором %d не найден", friendId));
        }

        Collection<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        commonFilms.forEach(this::sortFilmCollections);

        log.info("Найдено {} общих фильмов у пользователей {} и {}",
                 commonFilms.size(), userId, friendId);
        return commonFilms;
    }

    private Set<Long> extractGenreIds(Set<Genre> genres) {
        return genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> extractDirectorIds(Set<Director> directors) {
        return directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
    }

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