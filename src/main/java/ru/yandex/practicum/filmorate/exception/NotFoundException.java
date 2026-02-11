package ru.yandex.practicum.filmorate.exception;

// Исключение если объект не найден
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
