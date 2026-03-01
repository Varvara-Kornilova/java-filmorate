
DELETE FROM likes;
DELETE FROM film_genres;
DELETE FROM friendship;
DELETE FROM films;
DELETE FROM users;



MERGE INTO mpa_rating (code, description) KEY (code) VALUES
    ('G', 'Для любой возрастной аудитории'),
    ('PG', 'Детям рекомендуется смотреть с родителями'),
    ('PG-13', 'Детям до 13 лет просмотр не желателен'),
    ('R', 'Лицам до 17 лет обязательно присутствие родителя'),
    ('NC-17', 'Лицам до 18 лет просмотр запрещён');


MERGE INTO genres (name) KEY (name) VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');


MERGE INTO friendship_status (name) KEY (name) VALUES
    ('НЕПОДТВЕРЖДЕННАЯ'),
    ('ПОДТВЕРЖДЁННАЯ');