package org.dplay.server.domain.user;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSaver {

    private final UserRepository userRepository;

    public User save(final User user){
        return userRepository.save(user);
    }

}
