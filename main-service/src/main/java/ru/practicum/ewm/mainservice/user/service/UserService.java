package ru.practicum.ewm.mainservice.user.service;

import ru.practicum.ewm.mainservice.user.dto.NewUserRequest;
import ru.practicum.ewm.mainservice.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size);

    UserDto registerUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);
}
