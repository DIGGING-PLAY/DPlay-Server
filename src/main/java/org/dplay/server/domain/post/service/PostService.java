package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostDto;
import org.dplay.server.domain.post.dto.PostResultDto;
import org.dplay.server.domain.post.dto.UserPostsResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.user.entity.User;

public interface PostService {
    PostDto createPost(final long userId, String trackId, String songTitle, String artistName, String coverImg, String isrc, String content);

    void deletePost(User user);

    void deletePostByPostId(final long userId, final long postId);

    void incrementLikeCount(Post post);

    void decrementLikeCount(Post post);

    void incrementSaveCount(Post post);

    void decrementSaveCount(Post post);

    Post findByPostId(long postId);

    PostResultDto getPostDetailByPostId(long postId, long userId);

    UserPostsResultDto getUserPosts(Long userId, String cursor, Integer limit);
}
