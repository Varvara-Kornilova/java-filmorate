-- ===========================================
-- Очистка таблиц перед заполнением
-- ===========================================
DELETE FROM film_genres;
DELETE FROM likes;
DELETE FROM friendship;
DELETE FROM films;
DELETE FROM users;

-- ===========================================
-- Сброс счётчиков ID (опционально, для чистоты)
-- ===========================================
ALTER TABLE film_genres ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE mpa_rating ALTER COLUMN rating_id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE friendship_status ALTER COLUMN status_id RESTART WITH 1;

-- ===========================================
-- Заполнение справочников (простые INSERT)
-- ===========================================

-- MPA рейтинги
INSERT INTO mpa_rating (name, description) VALUES
    ('G', 'Для любой возрастной аудитории'),
    ('PG', 'Детям рекомендуется смотреть с родителями'),
    ('PG-13', 'Детям до 13 лет просмотр не желателен'),
    ('R', 'Лицам до 17 лет обязательно присутствие родителя'),
    ('NC-17', 'Лицам до 18 лет просмотр запрещён');

-- Статусы дружбы
INSERT INTO friendship_status (name) VALUES
    ('НЕПОДТВЕРЖДЕННАЯ'),
    ('ПОДТВЕРЖДЁННАЯ');

-- Жанры
INSERT INTO genres (name) VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');