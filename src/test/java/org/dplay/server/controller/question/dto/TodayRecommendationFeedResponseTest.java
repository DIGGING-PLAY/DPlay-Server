package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TodayRecommendationFeedResponseTest {

    @Test
    @DisplayName("에디터픽만 존재하면 순서대로 배지를 부여한다")
    void from_onlyEditorPicks_assignsSequentialBadges() {
        List<PostFeedItemDto> items = List.of(
                createItem(1L, true, 0, LocalDateTime.of(2025, 11, 5, 8, 0)),
                createItem(2L, true, 0, LocalDateTime.of(2025, 11, 5, 8, 5)),
                createItem(3L, true, 0, LocalDateTime.of(2025, 11, 5, 8, 10))
        );

        PostFeedResultDto resultDto = new PostFeedResultDto(
                20251105L,
                LocalDate.of(2025, 11, 5),
                "야식 먹기 전 듣는 노래는?",
                false,
                true,
                3,
                3L,
                null,
                items
        );

        TodayRecommendationFeedResponse response = TodayRecommendationFeedResponse.from(resultDto);

        assertThat(response.items()).hasSize(3);
        assertThat(response.items().get(0).badges()).satisfies(badges -> {
            assertThat(badges.isEditorPick()).isTrue();
            assertThat(badges.isPopular()).isFalse();
            assertThat(badges.isNew()).isFalse();
        });
        assertThat(response.items().get(1).badges()).satisfies(badges -> {
            assertThat(badges.isEditorPick()).isTrue();
            assertThat(badges.isPopular()).isTrue();
            assertThat(badges.isNew()).isFalse();
        });
        assertThat(response.items().get(2).badges()).satisfies(badges -> {
            assertThat(badges.isEditorPick()).isTrue();
            assertThat(badges.isPopular()).isFalse();
            assertThat(badges.isNew()).isTrue();
        });
    }

    @Test
    @DisplayName("가장 많은 좋아요와 최신 게시물을 배지로 표기한다")
    void from_marksPopularAndNewWhenUserPostsExist() {
        PostFeedItemDto editorPick = createItem(1L, true, 5, LocalDateTime.of(2025, 11, 5, 7, 0));
        PostFeedItemDto popularUserPost = createItem(10L, false, 15, LocalDateTime.of(2025, 11, 5, 9, 0));
        PostFeedItemDto newestUserPost = createItem(11L, false, 8, LocalDateTime.of(2025, 11, 5, 10, 0));

        PostFeedResultDto resultDto = new PostFeedResultDto(
                20251105L,
                LocalDate.of(2025, 11, 5),
                "야식 먹기 전 듣는 노래는?",
                true,
                false,
                3,
                3L,
                null,
                List.of(editorPick, popularUserPost, newestUserPost)
        );

        TodayRecommendationFeedResponse response = TodayRecommendationFeedResponse.from(resultDto);

        assertThat(response.items()).hasSize(3);
        assertThat(response.items().get(0).badges().isEditorPick()).isTrue();
        assertThat(response.items().get(1).badges().isPopular()).isTrue();
        assertThat(response.items().get(2).badges().isNew()).isTrue();
    }

    private PostFeedItemDto createItem(Long postId,
                                       boolean isEditorPick,
                                       int likeCount,
                                       LocalDateTime createdAt) {
        Question question = Question.builder()
                .title("question")
                .displayDate(createdAt.toLocalDate())
                .postCount(0)
                .build();

        Track track = Track.builder()
                .trackId("apple:" + postId)
                .songTitle("Song " + postId)
                .artistName("Artist " + postId)
                .coverImg("cover" + postId)
                .build();

        User user = User.builder()
                .nickname("user" + postId)
                .profileImg("profile" + postId)
                .platformId("platform" + postId)
                .platform(Platform.KAKAO)
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("content" + postId)
                .likeCount(likeCount)
                .saveCount(0)
                .build();

        ReflectionTestUtils.setField(post, "postId", postId);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);

        return new PostFeedItemDto(post, isEditorPick, false, false, false, false);
    }
}
