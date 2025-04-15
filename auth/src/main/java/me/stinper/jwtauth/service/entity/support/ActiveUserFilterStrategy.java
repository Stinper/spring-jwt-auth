package me.stinper.jwtauth.service.entity.support;

import me.stinper.jwtauth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.lang.NonNull;

import java.util.List;

public class ActiveUserFilterStrategy implements UserFilterStrategy {
    @Override
    public Page<User> filterUsersPage(@NonNull Page<User> pageToFilter) {
        List<User> activeUsers = pageToFilter.getContent().stream()
                .filter(user -> user.getDeactivatedAt() == null)
                .toList();

        return new PageImpl<>(activeUsers, pageToFilter.getPageable(), activeUsers.size());
    }
}
