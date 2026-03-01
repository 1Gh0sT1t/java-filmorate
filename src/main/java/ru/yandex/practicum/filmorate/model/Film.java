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

    private Set<Integer> likes = new HashSet<>();

    // Жанры фильма (несколько)
    private Set<Genre> genres = new HashSet<>();

    // Рейтинг MPA
    @NotNull(message = "MPA рейтинг обязателен")
    private MpaRating mpaRating;

    // Проверка даты релиза (не раньше 28.12.1895)
    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return false;
        }
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
