package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {

    // Работаем с хранилищем фильмов
    private final FilmStorage filmStorage;

    // Нужно чтобы проверять существование пользователя
    private final UserStorage userStorage;

    // Хранилище рейтингов MPA
    private final MpaStorage mpaStorage;

    // Хранилище жанров
    private final GenreStorage genreStorage;

    // Минимальная допустимая дата релиза
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    // Создаем новый фильм
    public Film create(Film film) {
        validateFilm(film);
        validateMpa(film);      // проверяем существование рейтинга
        validateGenres(film);   // проверяем существование жанров
        return filmStorage.create(film);
    }

    // Обновляем фильм
    public Film update(Film film) {
        validateFilm(film);
        validateMpa(film);
        validateGenres(film);
        return filmStorage.update(film);
    }

    // Получаем фильм по id
    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    // Получаем список всех фильмов
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    // Добавляем лайк фильму
    public void addLike(int filmId, int userId) {
        userStorage.getById(userId); // проверяем что пользователь существует
        filmStorage.addLike(filmId, userId);
    }

    // Удаляем лайк у фильма
    public void removeLike(int filmId, int userId) {
        userStorage.getById(userId); // проверяем что пользователь существует
        filmStorage.removeLike(filmId, userId);
    }

    // Получаем популярные фильмы
    public Collection<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    // Проверяем корректность фильма
    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза слишком ранняя");
        }
    }

    // Проверяем что указанный рейтинг MPA существует
    private void validateMpa(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.findById(film.getMpa().getId());
        }
    }

    // Проверяем что указанные жанры существуют
    private void validateGenres(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId());
            }
        }
    }
}