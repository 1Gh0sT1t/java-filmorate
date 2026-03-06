package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
    }

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@test.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(20));

        User created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals("login", created.getLogin());
    }

    @Test
    void shouldAddFriend() {
        User u1 = new User();
        u1.setEmail("a@a.ru");
        u1.setLogin("a");
        u1.setBirthday(LocalDate.now().minusYears(20));

        User u2 = new User();
        u2.setEmail("b@b.ru");
        u2.setLogin("b");
        u2.setBirthday(LocalDate.now().minusYears(20));

        userService.create(u1);
        userService.create(u2);

        userService.addFriend(u1.getId(), u2.getId());

        assertEquals(1, userService.getFriends(u1.getId()).size());
    }

    @Test
    void shouldRemoveFriend() {
        User u1 = new User();
        u1.setEmail("a@a.ru");
        u1.setLogin("a");
        u1.setBirthday(LocalDate.now().minusYears(20));

        User u2 = new User();
        u2.setEmail("b@b.ru");
        u2.setLogin("b");
        u2.setBirthday(LocalDate.now().minusYears(20));

        userService.create(u1);
        userService.create(u2);

        userService.addFriend(u1.getId(), u2.getId());
        userService.removeFriend(u1.getId(), u2.getId());

        assertEquals(0, userService.getFriends(u1.getId()).size());
    }

    @Test
    void shouldReturnCommonFriends() {
        User u1 = new User();
        u1.setEmail("1@1.ru");
        u1.setLogin("u1");
        u1.setBirthday(LocalDate.now().minusYears(20));

        User u2 = new User();
        u2.setEmail("2@2.ru");
        u2.setLogin("u2");
        u2.setBirthday(LocalDate.now().minusYears(20));

        User u3 = new User();
        u3.setEmail("3@3.ru");
        u3.setLogin("u3");
        u3.setBirthday(LocalDate.now().minusYears(20));

        userService.create(u1);
        userService.create(u2);
        userService.create(u3);

        userService.addFriend(u1.getId(), u3.getId());
        userService.addFriend(u2.getId(), u3.getId());

        assertEquals(1,
                userService.getCommonFriends(u1.getId(), u2.getId()).size());
    }


    @Test
    void shouldThrowIfUserNotFound() {
        assertThrows(RuntimeException.class,
                () -> userService.getFriends(999));
    }

}
