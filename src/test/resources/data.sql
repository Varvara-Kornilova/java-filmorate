-- ===========================================
-- ПОЛНАЯ ОЧИСТКА БАЗЫ ПЕРЕД ТЕСТАМИ
-- ===========================================

-- Сначала дочерние таблицы (чтобы не нарушить FK)
DELETE FROM film_genres;
DELETE FROM likes;
DELETE FROM friendship;

-- Затем основные таблицы
DELETE FROM films;
DELETE FROM users;

-- Справочники + сброс ID
DELETE FROM mpa_rating;
ALTER TABLE mpa_rating ALTER COLUMN rating_id RESTART WITH 1;

DELETE FROM friendship_status;
ALTER TABLE friendship_status ALTER COLUMN status_id RESTART WITH 1;

DELETE FROM genres;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;

-- Сброс ID в основных таблицах
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;

-- ===========================================
-- Заполнение справочников
-- ===========================================

INSERT INTO mpa_rating (name, description) VALUES
    ('G', 'Для любой возрастной аудитории'),
    ('PG', 'Детям рекомендуется смотреть с родителями'),
    ('PG-13', 'Детям до 13 лет просмотр не желателен'),
    ('R', 'Лицам до 17 лет обязательно присутствие родителя'),
    ('NC-17', 'Лицам до 18 лет просмотр запрещён');

INSERT INTO friendship_status (name) VALUES
    ('НЕПОДТВЕРЖДЕННАЯ'),
    ('ПОДТВЕРЖДЁННАЯ');

INSERT INTO genres (name) VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');