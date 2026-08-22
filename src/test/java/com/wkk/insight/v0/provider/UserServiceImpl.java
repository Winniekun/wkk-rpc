package com.wkk.insight.v0.provider;

import com.wkk.insight.api.UserService;
import com.wkk.insight.domain.User;

import java.util.UUID;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Integer id) {
        return User.builder()
                .id(id)
                .sex("")
                .userName(UUID.randomUUID().toString())
                .build();
    }
}
