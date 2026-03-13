package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Repository
@Primary
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    // Получаем список всех жанров из базы
    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        });
    }

    // Получаем жанр по id
    @Override
    public Genre findById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }

    // Получаем жанры сразу по набору id
    @Override
    public Map<Integer, Genre> findByIds(Set<Integer> ids) {
        Map<Integer, Genre> genresById = new LinkedHashMap<>();

        if (ids == null || ids.isEmpty()) {
            return genresById;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, name FROM genres WHERE id IN (" + placeholders + ") ORDER BY id";

        jdbcTemplate.query(sql, rs -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            genresById.put(genre.getId(), genre);
        }, ids.toArray());

        return genresById;
    }
}