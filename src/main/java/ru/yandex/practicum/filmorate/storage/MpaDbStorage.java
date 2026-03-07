package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Repository
@Primary
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    // Получаем список всех рейтингов MPA
    @Override
    public Collection<Mpa> findAll() {
        String sql = "SELECT id, code AS name FROM mpa_ratings ORDER BY id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });
    }

    // Получаем рейтинг по id
    @Override
    public Mpa findById(int id) {
        String sql = "SELECT id, code AS name FROM mpa_ratings WHERE id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                }, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }
}