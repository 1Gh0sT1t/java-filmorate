package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Component
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
}