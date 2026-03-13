package com.server.organization.infrastructure.users;

import com.server.organization.api.UserDTO;
import com.server.organization.domain.enums.AccountStatus;
import com.server.organization.domain.users.User;
import com.server.organization.domain.users.UserEmail;
import com.server.organization.domain.users.UserPassword;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail().value(),
                user.getFullName(),
                user.getGlobalRole().name(),
                user.getAccountStatus().name()
        );
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getEmail().value(),
                user.getPassword().value(),
                user.getFullName(),
                user.getGlobalRole(),
                user.getAccountStatus()
        );
        entity.setId(user.getId());
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        AccountStatus status = entity.getAccountStatus() != null
                ? entity.getAccountStatus()
                : AccountStatus.ACTIVE;
        return new User(
                entity.getId(),
                new UserEmail(entity.getEmail()),
                new UserPassword(entity.getPassword()),
                entity.getFullName(),
                entity.getGlobalRole(),
                status
        );
    }
}
