package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // Сохраняем жанры фильма
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

        // Удаляем старые жанры фильма
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        // Сохраняем новые жанры фильма
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
                ORDER BY COUNT(l.user_id) DESC, f.id
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

            return film;
        }, count);
    }

    // Удаляем фильм
    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    // Сохраняем жанры фильма
    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        film.getGenres().stream()
                .map(genre -> genre.getId())
                .distinct()
                .sorted()
                .forEach(genreId -> jdbcTemplate.update(sql, film.getId(), genreId));
    }

    // Получаем id жанров сразу для списка фильмов
    public Map<Integer, Set<Integer>> getGenreIdsByFilmIds(Collection<Integer> filmIds) {
        Map<Integer, Set<Integer>> genreIdsByFilmId = new LinkedHashMap<>();

        for (Integer filmId : filmIds) {
            genreIdsByFilmId.put(filmId, new LinkedHashSet<>());
        }

        if (filmIds == null || filmIds.isEmpty()) {
            return genreIdsByFilmId;
        }

        String inSql = String.join(",", java.util.Collections.nCopies(filmIds.size(), "?"));
        String sql = """
                SELECT film_id, genre_id
                FROM film_genres
                WHERE film_id IN (%s)
                ORDER BY film_id, genre_id
                """.formatted(inSql);

        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int genreId = rs.getInt("genre_id");
            genreIdsByFilmId.computeIfAbsent(filmId, key -> new LinkedHashSet<>()).add(genreId);
        }, filmIds.toArray());

        return genreIdsByFilmId;
    }

    // Получаем лайки сразу для списка фильмов
    public Map<Integer, Set<Integer>> getLikesByFilmIds(Collection<Integer> filmIds) {
        Map<Integer, Set<Integer>> likesByFilmId = new LinkedHashMap<>();

        for (Integer filmId : filmIds) {
            likesByFilmId.put(filmId, new LinkedHashSet<>());
        }

        if (filmIds == null || filmIds.isEmpty()) {
            return likesByFilmId;
        }

        String inSql = String.join(",", java.util.Collections.nCopies(filmIds.size(), "?"));
        String sql = """
                SELECT film_id, user_id
                FROM film_likes
                WHERE film_id IN (%s)
                ORDER BY film_id, user_id
                """.formatted(inSql);

        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            likesByFilmId.computeIfAbsent(filmId, key -> new LinkedHashSet<>()).add(userId);
        }, filmIds.toArray());

        return likesByFilmId;
    }
}