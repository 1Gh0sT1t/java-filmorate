package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private int id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    // Рейтинг MPA фильма
    @NotNull(message = "Рейтинг MPA обязателен")
    private Mpa mpa;

    // Список жанров фильма
    private Set<Genre> genres = new HashSet<>();

    // Лайки пользователей (id пользователей)
    private Set<Integer> likes = new HashSet<>();

    // Проверяем что дата релиза не раньше появления кино
    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return false;
        }
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}