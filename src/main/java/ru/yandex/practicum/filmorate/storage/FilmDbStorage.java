package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    // JdbcTemplate используется для выполнения SQL-запросов к базе данных
    private final JdbcTemplate jdbcTemplate;

    // Создание нового фильма
    @Override
    public Film create(Film film) {

        // Проверяем существование рейтинга MPA
        validateMpa(film.getMpa().getId());

        // Проверяем существование жанров
        validateGenres(film.getGenres());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId());

        // Получаем id созданного фильма
        Integer id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM films", Integer.class);
        film.setId(id);

        // Сохраняем жанры фильма в таблицу связей
        saveGenres(film);

        return film;
    }

    // Обновление существующего фильма
    @Override
    public Film update(Film film) {

        // Проверяем существование рейтинга MPA
        validateMpa(film.getMpa().getId());

        // Проверяем существование жанров
        validateGenres(film.getGenres());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        // Если фильм не найден — выбрасываем исключение
        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        // Удаляем старые жанры фильма
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        // Сохраняем новые жанры
        saveGenres(film);

        return film;
    }

    // Получение фильма по id
    @Override
    public Film getById(int id) {
        String sql = """
                SELECT f.*, m.code AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();

            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        }, id);

        // Если фильм не найден — выбрасываем исключение
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        Film film = films.get(0);

        // Загружаем жанры фильма
        film.setGenres(getGenres(film.getId()));

        // Загружаем лайки фильма
        film.setLikes(getLikes(film.getId()));

        return film;
    }

    // Получение списка всех фильмов
    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.*, m.code AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();

            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        });

        // Для каждого фильма подгружаем жанры и лайки
        for (Film film : films) {
            film.setGenres(getGenres(film.getId()));
            film.setLikes(getLikes(film.getId()));
        }

        return films;
    }

    // Добавление лайка фильму
    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    // Удаление лайка у фильма
    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    // Получение списка популярных фильмов
    @Override
    public Collection<Film> getPopular(int count) {
        String sql = """
                SELECT f.id
                FROM films f
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class, count);

        List<Film> films = new ArrayList<>();

        // Получаем полную информацию по каждому фильму
        for (Integer id : ids) {
            films.add(getById(id));
        }

        return films;
    }

    // Удаление фильма по id
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    // Сохранение жанров фильма в таблицу film_genres
    private void saveGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    // Получение жанров фильма
    private Set<Genre> getGenres(int filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId));
    }

    // Получение списка пользователей, поставивших лайк фильму
    private Set<Integer> getLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }

    // Проверка существования рейтинга MPA
    private void validateMpa(int mpaId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?",
                Integer.class,
                mpaId
        );

        if (count == null || count == 0) {
            throw new NotFoundException("MPA с id " + mpaId + " не найден");
        }
    }

    // Проверка существования жанров
    private void validateGenres(Set<Genre> genres) {
        if (genres == null) {
            return;
        }

        for (Genre genre : genres) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM genres WHERE id = ?",
                    Integer.class,
                    genre.getId()
            );

            if (count == null || count == 0) {
                throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
            }
        }
    }
}