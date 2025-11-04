package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostDto;

public interface PostService {
    PostDto createPost(Long userId, String trackId, String songTitle, String artistName, String coverImg, String isrc, String content);
}
