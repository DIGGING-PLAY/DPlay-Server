package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.post.dto.PostDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.repository.PostRepository;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.service.TrackService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TrackService trackService;
    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public PostDto createPost(final long userId, String trackId, String songTitle, String artistName, String coverImg, String isrc, String content) {
        LocalDate today = LocalDate.now(clock);
        Question question = questionService.getQuestionByDate(today);

        // TODO: 추후 UserService 구현 시 UserService.getUser(userId)로 변경
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        Track track = trackService.createTrackByPost(trackId, songTitle, artistName, coverImg, isrc);

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content(content)
                .likeCount(0)
                .saveCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        return PostDto.of(savedPost);
    }

    @Override
    @Transactional
    public void deletePostByPostId(final long userId, final long postId) {
        Post post = findByPostId(postId);

        isPostUser(userId, post);

        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void incrementLikeCount(Post post, final long userId) {
        post.incrementLikeCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void decrementLikeCount(Post post, final long userId) {
        post.decrementLikeCount();
        postRepository.save(post);
    }

    @Override
    public Post findByPostId(final long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new DPlayException(ResponseError.TARGET_NOT_FOUND));
    }

    private void isPostUser(final long userId, Post post) {
        if (!post.getUser().getUserId().equals(userId)) {
            throw new DPlayException(ResponseError.FORBIDDEN_RESOURCE);
        }
    }
}

