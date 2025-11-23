package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.post.dto.PostDto;
import org.dplay.server.domain.post.dto.PostLikeResultDto;
import org.dplay.server.domain.post.dto.PostResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.repository.PostLikeRepository;
import org.dplay.server.domain.post.repository.PostRepository;
import org.dplay.server.domain.post.repository.PostSaveRepository;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.service.QuestionEditorPickService;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.domain.track.dto.TrackDetailResultDto;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.service.TrackService;
import org.dplay.server.domain.user.dto.UserDetailResultDto;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
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
    private final PostLikeRepository postLikeRepository;
    private final PostSaveRepository postSaveRepository;
    private final QuestionEditorPickService questionEditorPickService;
    private final TrackService trackService;
    private final QuestionService questionService;
    private final Clock clock;
    private final UserService userService;
    private static final String DEFAULT_STOREFRONT = "kr";

    @Override
    @Transactional
    public PostDto createPost(final long userId, String trackId, String songTitle, String artistName, String coverImg, String isrc, String content) {
        LocalDate today = LocalDate.now(clock);
        Question question = questionService.getQuestionByDate(today);
        User user = userService.getUserById(userId);

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
    public void incrementLikeCount(Post post) {
        post.incrementLikeCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void decrementLikeCount(Post post) {
        post.decrementLikeCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void incrementSaveCount(Post post) {
        post.incrementSaveCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void decrementSaveCount(Post post) {
        post.decrementSaveCount();
        postRepository.save(post);
    }

    @Override
    public Post findByPostId(final long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new DPlayException(ResponseError.TARGET_NOT_FOUND));
    }

    @Override
    public PostResultDto getPostDetailByPostId(final long postId, final long userId) {
        Post post = findByPostId(postId);

        boolean isEditorPick = questionEditorPickService.existsByPost(post);
        Question postQuestion = post.getQuestion();

        if (!isEditorPick) {
            boolean hasWrittenPostForQuestion = postRepository.existsByQuestionQuestionIdAndUserUserId(
                    postQuestion.getQuestionId(),
                    userId
            );

            if (!hasWrittenPostForQuestion) {
                throw new DPlayException(ResponseError.FORBIDDEN_RESOURCE);
            }
        }

        User currentUser = userService.getUserById(userId);
        User postAuthor = post.getUser();
        UserDetailResultDto userDetailResultDto = UserDetailResultDto.from(postAuthor);

        TrackDetailResultDto trackDetailResultDto = trackService.getTrackDetail(
                post.getTrack().getTrackId(),
                DEFAULT_STOREFRONT
        );

        boolean isHost = isPostHost(userId, post);
        boolean isScrapped = postSaveRepository.existsByPostAndUser(post, currentUser);
        boolean isLiked = postLikeRepository.existsByPostAndUser(post, currentUser);
        PostLikeResultDto postLikeResultDto = PostLikeResultDto.of(isLiked, post);

        return PostResultDto.of(
                post,
                isHost,
                isScrapped,
                trackDetailResultDto,
                userDetailResultDto,
                postLikeResultDto
        );
    }

    private boolean isPostHost(final long userId, Post post) {
        return post.getUser().getUserId().equals(userId);
    }

    private void isPostUser(final long userId, Post post) {
        if (!post.getUser().getUserId().equals(userId)) {
            throw new DPlayException(ResponseError.FORBIDDEN_RESOURCE);
        }
    }
}

