package org.dplay.server.domain.user.facade;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final PostService postService;
    private final UserService userService;

    @Transactional
    public void deleteUser(Long userId) {
        User user = userService.getUserById(userId);

        postService.deletePost(user);
        userService.deleteUser(userId);
    }
}
