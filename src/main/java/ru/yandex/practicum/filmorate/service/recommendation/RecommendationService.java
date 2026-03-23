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
        log.info("getRecommendations вызван для userId={}", userId);

        try {

            Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();

            if (!userLikes.containsKey(userId) || userLikes.get(userId).isEmpty()) {
                log.info("У пользователя {} нет лайков", userId);
                return Collections.emptyList();
            }

            Set<Long> currentUserLikes = userLikes.get(userId);

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

                if (common > maxCommon) {
                    maxCommon = common;
                    bestMatch = otherUserId;
                }
            }

            if (bestMatch == null) {
                log.info("Не найден похожий пользователь для {}", userId);
                return Collections.emptyList();
            }

            Set<Long> bestMatchLikes = userLikes.get(bestMatch);
            Set<Long> recommendedFilmIds = new HashSet<>(bestMatchLikes);
            recommendedFilmIds.removeAll(currentUserLikes);

            if (recommendedFilmIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<Film> recommendations = new ArrayList<>();
            for (Long filmId : recommendedFilmIds) {
                filmStorage.findById(filmId).ifPresent(recommendations::add);
            }

            log.info("Найдено {} рекомендаций для пользователя {}", recommendations.size(), userId);
            return recommendations;

        } catch (Exception e) {
            log.error("Ошибка в getRecommendations: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
