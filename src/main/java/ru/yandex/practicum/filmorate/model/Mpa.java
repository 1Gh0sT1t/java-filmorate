package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Mpa {

    private int id;

    // Код рейтинга (G, PG, PG-13 и т.д.)
    private String name;
}