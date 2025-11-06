package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostDto;

public interface PostService {
    PostDto createPost(final long userId, String trackId, String songTitle, String artistName, String coverImg, String isrc, String content);

    void deletePostByPostId(final long userId, final long postId);
}
