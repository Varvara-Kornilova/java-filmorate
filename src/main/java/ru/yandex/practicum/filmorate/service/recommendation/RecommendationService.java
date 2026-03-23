package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.util.*;

@Slf4j
@Service
public class RecommendationService {

    private final LikeStorage likeStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(LikeStorage likeStorage, FilmStorage filmStorage) {
        this.likeStorage = likeStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getRecommendations(Long userId) {
        log.info("=== НАЧАЛО getRecommendations для userId: {} ===", userId);

        try {
            log.info("Шаг 1: Получение всех лайков пользователей");
            Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();
            log.info("Шаг 1 завершён: получено {} пользователей с лайками", userLikes.size());

            log.info("Шаг 2: Проверка наличия лайков у пользователя {}", userId);
            if (!userLikes.containsKey(userId)) {
                log.info("Пользователь {} не найден в userLikes", userId);
                return Collections.emptyList();
            }

            Set<Long> currentUserLikes = userLikes.get(userId);
            log.info("У пользователя {} {} лайков: {}", userId, currentUserLikes.size(), currentUserLikes);

            if (currentUserLikes.isEmpty()) {
                log.info("У пользователя {} нет лайков", userId);
                return Collections.emptyList();
            }

            log.info("Шаг 3: Поиск похожего пользователя");
            Long bestMatch = null;
            int maxCommon = 0;

            for (Map.Entry<Long, Set<Long>> entry : userLikes.entrySet()) {
                Long otherUserId = entry.getKey();
                if (otherUserId.equals(userId)) continue;

                Set<Long> otherLikes = entry.getValue();
                int common = 0;
                for (Long filmId : otherLikes) {
                    if (currentUserLikes.contains(filmId)) common++;
                }

                log.debug("Пользователь {} имеет {} общих лайков с пользователем {}",
                         otherUserId, common, userId);

                if (common > maxCommon) {
                    maxCommon = common;
                    bestMatch = otherUserId;
                }
            }

            log.info("Шаг 3 завершён: найден пользователь {}, общих лайков: {}", bestMatch, maxCommon);

            if (bestMatch == null) {
                log.info("Не найден похожий пользователь для {}", userId);
                return Collections.emptyList();
            }

            log.info("Шаг 4: Получение фильмов для рекомендации");
            Set<Long> bestMatchLikes = userLikes.get(bestMatch);
            Set<Long> recommendedFilmIds = new HashSet<>(bestMatchLikes);
            recommendedFilmIds.removeAll(currentUserLikes);

            log.info("Рекомендуемые filmId: {}", recommendedFilmIds);

            if (recommendedFilmIds.isEmpty()) {
                log.info("Нет новых фильмов для рекомендации пользователю {}", userId);
                return Collections.emptyList();
            }

            log.info("Шаг 5: Загрузка полной информации о фильмах");
            List<Film> recommendations = new ArrayList<>();
            for (Long filmId : recommendedFilmIds) {
                log.debug("Загрузка фильма с id: {}", filmId);
                filmStorage.findById(filmId).ifPresent(recommendations::add);
            }

            log.info("Шаг 5 завершён: загружено {} фильмов", recommendations.size());
            log.info("=== УСПЕШНО найдено {} рекомендаций для пользователя {} ===",
                     recommendations.size(), userId);
            return recommendations;

        } catch (Exception e) {
            log.error("!!! КРИТИЧЕСКАЯ ОШИБКА в getRecommendations для пользователя {}: {}",
                      userId, e.getMessage(), e);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
