package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.util.*;
import java.util.stream.Collectors;

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
        log.debug("Расчет рекомендаций для пользователя {}", userId);

        Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();

        if (!userLikes.containsKey(userId) || userLikes.get(userId).isEmpty()) {
            log.debug("У пользователя {} нет лайков", userId);
            return Collections.emptyList();
        }

        SlopeOneMatrices matrices = buildSlopeOneMatrices(userLikes);

        Map<Long, Double> predictions = predictRatings(userId, userLikes, matrices);

        List<Long> recommendedFilmIds = predictions.entrySet().stream()
                .filter(e -> e.getValue() > 0.5)
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (recommendedFilmIds.isEmpty()) {
            log.debug("Нет фильмов для рекомендации пользователю {}", userId);
            return Collections.emptyList();
        }

        List<Film> recommendations = new ArrayList<>();
        for (Long filmId : recommendedFilmIds) {
            filmStorage.findById(filmId).ifPresent(recommendations::add);
        }

        log.info("Найдено {} рекомендаций для пользователя {}", recommendations.size(), userId);
        return recommendations;
    }

    private SlopeOneMatrices buildSlopeOneMatrices(Map<Long, Set<Long>> userLikes) {

        Map<Long, Map<Long, Double>> diff = new HashMap<>();
        Map<Long, Map<Long, Integer>> freq = new HashMap<>();

        for (Set<Long> likes : userLikes.values()) {
            List<Long> likedFilms = new ArrayList<>(likes);

            for (int i = 0; i < likedFilms.size(); i++) {
                Long film1 = likedFilms.get(i);

                diff.putIfAbsent(film1, new HashMap<>());
                freq.putIfAbsent(film1, new HashMap<>());

                for (int j = 0; j < likedFilms.size(); j++) {
                    if (i == j) continue;
                    Long film2 = likedFilms.get(j);

                    double observedDiff = 0.0;

                    int oldCount = freq.get(film1).getOrDefault(film2, 0);
                    double oldDiff = diff.get(film1).getOrDefault(film2, 0.0);

                    freq.get(film1).put(film2, oldCount + 1);
                    diff.get(film1).put(film2, oldDiff + observedDiff);
                }
            }
        }

        for (Long film1 : diff.keySet()) {
            for (Long film2 : diff.get(film1).keySet()) {
                int count = freq.get(film1).get(film2);
                double avgDiff = diff.get(film1).get(film2) / count;
                diff.get(film1).put(film2, avgDiff);
            }
        }

        return new SlopeOneMatrices(diff, freq);
    }

    private Map<Long, Double> predictRatings(Long userId,
                                              Map<Long, Set<Long>> userLikes,
                                              SlopeOneMatrices matrices) {
        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Integer> weights = new HashMap<>();

        Set<Long> userLikedFilms = userLikes.getOrDefault(userId, Collections.emptySet());

        for (Long likedFilm : userLikedFilms) {

            Map<Long, Double> filmDiffs = matrices.diff.getOrDefault(likedFilm, Collections.emptyMap());
            Map<Long, Integer> filmFreqs = matrices.freq.getOrDefault(likedFilm, Collections.emptyMap());

            for (Map.Entry<Long, Double> entry : filmDiffs.entrySet()) {
                Long targetFilm = entry.getKey();
                if (userLikedFilms.contains(targetFilm)) {
                    continue;
                }

                double diffValue = entry.getValue();
                int frequency = filmFreqs.getOrDefault(targetFilm, 0);

                double predictedRating = 1.0 + diffValue;

                predictions.put(targetFilm, predictions.getOrDefault(targetFilm, 0.0) + predictedRating * frequency);
                weights.put(targetFilm, weights.getOrDefault(targetFilm, 0) + frequency);
            }
        }

        Map<Long, Double> averagedPredictions = new HashMap<>();
        for (Long filmId : predictions.keySet()) {
            int weight = weights.get(filmId);
            if (weight > 0) {
                averagedPredictions.put(filmId, predictions.get(filmId) / weight);
            }
        }

        return averagedPredictions;
    }

    private static class SlopeOneMatrices {
        final Map<Long, Map<Long, Double>> diff;
        final Map<Long, Map<Long, Integer>> freq;

        SlopeOneMatrices(Map<Long, Map<Long, Double>> diff, Map<Long, Map<Long, Integer>> freq) {
            this.diff = diff;
            this.freq = freq;
        }
    }
}
