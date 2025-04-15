package me.stinper.jwtauth.service.entity.support;

import me.stinper.jwtauth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

public class AllUserFilterStrategy implements UserFilterStrategy {
    @Override
    public Page<User> filterUsersPage(@NonNull Page<User> pageToFilter) {
        return pageToFilter; //Выбор всех пользователей (фильтрация отсутствует)
    }
}
