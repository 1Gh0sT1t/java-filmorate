package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    // Создаем новый фильм
    @Override
    public Film create(Film film) {

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

        // сохраняем жанры
        saveGenres(film);

        return film;
    }

    // Обновляем фильм
    @Override
    public Film update(Film film) {

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        // удаляем старые жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        // сохраняем новые
        saveGenres(film);

        return film;
    }

    // Получаем фильм по id
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

            // загружаем жанры
            film.setGenres(getGenresByFilmId(film.getId()));

            // загружаем лайки
            film.setLikes(getLikesByFilmId(film.getId()));

            return film;

        }, id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        return films.get(0);
    }

    // Получаем список всех фильмов
    @Override
    public Collection<Film> findAll() {

        String sql = """
                SELECT f.*, m.code AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

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

            // загружаем жанры
            film.setGenres(getGenresByFilmId(film.getId()));

            // загружаем лайки
            film.setLikes(getLikesByFilmId(film.getId()));

            return film;

        });
    }

    // Добавляем лайк
    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    // Удаляем лайк
    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    // Получаем популярные фильмы
    @Override
    public Collection<Film> getPopular(int count) {

        String sql = """
                SELECT f.*, m.code AS mpa_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

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

            // загружаем жанры
            film.setGenres(getGenresByFilmId(film.getId()));

            // загружаем лайки
            film.setLikes(getLikesByFilmId(film.getId()));

            return film;

        }, count);
    }

    // Удаляем фильм
    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    private void saveGenres(Film film) {

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        film.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .forEach(genre ->
                        jdbcTemplate.update(sql, film.getId(), genre.getId()));
    }

    private Set<Genre> getGenresByFilmId(int filmId) {

        String sql = """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {

            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));

            return genre;

        }, filmId);

        return new LinkedHashSet<>(genres);
    }

    // Новый метод для получения лайков фильма
    private Set<Integer> getLikesByFilmId(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }
}