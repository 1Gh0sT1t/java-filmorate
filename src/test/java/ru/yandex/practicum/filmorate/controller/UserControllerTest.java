package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void shouldThrowExceptionIfEmailInvalid() {
        User user = new User();
        user.setEmail("invalid");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(20));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldSetLoginAsNameIfNameEmpty() {
        User user = new User();
        user.setEmail("test@test.ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.now().minusYears(20));

        User created = controller.create(user);

        assertEquals("login", created.getName());
    }

    @Test
    void shouldThrowExceptionIfBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@test.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }
}
