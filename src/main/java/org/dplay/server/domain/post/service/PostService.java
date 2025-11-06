package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostDto;
import org.dplay.server.domain.post.entity.Post;

public interface PostService {
    PostDto createPost(final long userId, String trackId, String songTitle, String artistName, String coverImg, String isrc, String content);

    void deletePostByPostId(final long userId, final long postId);

    void incrementLikeCount(Post post, final long userId);

    Post findByPostId(long postId);
}
