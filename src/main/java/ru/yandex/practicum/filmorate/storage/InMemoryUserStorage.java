package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    // здесь храним пользователей
    private final Map<Integer, User> users = new HashMap<>();

    // счетчик для id
    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("User not found");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    @Override
    public void addFriend(int id, int friendId) {
        User user = getById(id);
        User friend = getById(friendId);

        // Отправитель отправляет запрос
        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);

// У получателя появляется подтверждённая дружба после согласия
        if (friend.getFriends().containsKey(id) &&
                friend.getFriends().get(id) == FriendshipStatus.UNCONFIRMED) {

            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(id, FriendshipStatus.CONFIRMED);
        }
    }

    @Override
    public void removeFriend(int id, int friendId) {
        User user = getById(id);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    @Override
    public Set<User> getFriends(int id) {
        User user = getById(id);

        Set<User> result = new HashSet<>();
        for (Map.Entry<Integer, FriendshipStatus> entry : user.getFriends().entrySet()) {
            if (entry.getValue() == FriendshipStatus.CONFIRMED) {
                result.add(getById(entry.getKey()));
            }
        }
        return result;
    }

    @Override
    public Set<User> getCommonFriends(int id, int otherId) {
        User user = getById(id);
        User other = getById(otherId);

        Set<User> result = new HashSet<>();

        for (Map.Entry<Integer, FriendshipStatus> entry : user.getFriends().entrySet()) {

            Integer friendId = entry.getKey();

            if (entry.getValue() == FriendshipStatus.CONFIRMED &&
                    other.getFriends().get(friendId) == FriendshipStatus.CONFIRMED) {

                result.add(getById(friendId));
            }
        }

        return result;
    }
}
