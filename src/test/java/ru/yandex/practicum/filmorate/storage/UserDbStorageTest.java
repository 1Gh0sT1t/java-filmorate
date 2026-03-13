package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990,1,1));

        User created = userStorage.create(user);

        assertThat(created.getId()).isPositive();

        User fromDb = userStorage.getById(created.getId());

        assertThat(fromDb.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990,1,1));

        User created = userStorage.create(user);

        created.setName("Updated");

        userStorage.update(created);

        User updated = userStorage.getById(created.getId());

        assertThat(updated.getName()).isEqualTo("Updated");
    }

    @Test
    void shouldAddFriend() {
        User u1 = new User();
        u1.setEmail("1@test.com");
        u1.setLogin("u1");
        u1.setBirthday(LocalDate.of(1990,1,1));

        User u2 = new User();
        u2.setEmail("2@test.com");
        u2.setLogin("u2");
        u2.setBirthday(LocalDate.of(1990,1,1));

        userStorage.create(u1);
        userStorage.create(u2);

        userStorage.addFriend(u1.getId(), u2.getId());

        Set<User> friends = userStorage.getFriends(u1.getId());

        assertThat(friends).hasSize(1);
    }

    @Test
    void shouldReturnCommonFriends() {
        User u1 = new User();
        u1.setEmail("1@test.com");
        u1.setLogin("u1");
        u1.setBirthday(LocalDate.of(1990,1,1));

        User u2 = new User();
        u2.setEmail("2@test.com");
        u2.setLogin("u2");
        u2.setBirthday(LocalDate.of(1990,1,1));

        User u3 = new User();
        u3.setEmail("3@test.com");
        u3.setLogin("u3");
        u3.setBirthday(LocalDate.of(1990,1,1));

        userStorage.create(u1);
        userStorage.create(u2);
        userStorage.create(u3);

        userStorage.addFriend(u1.getId(), u3.getId());
        userStorage.addFriend(u2.getId(), u3.getId());

        Set<User> common = userStorage.getCommonFriends(u1.getId(), u2.getId());

        assertThat(common).hasSize(1);
    }

}