package com.communityplatform.users.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.communityplatform.users.dto.follow.FollowCountDto;
import com.communityplatform.users.dto.follow.FollowResponseDto;
import com.communityplatform.users.dto.user.UserSummaryDto;
import com.communityplatform.users.entity.FollowEntity;
import com.communityplatform.users.entity.UserEntity;
import com.communityplatform.users.exception.SelfFollowException;
import com.communityplatform.users.repository.FollowRepository;
import com.communityplatform.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowServiceImpl followService;

    @Test
    void followUserCreatesRelationship() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        followService.followUser(1L, 2L);

        verify(followRepository).save(any(FollowEntity.class));
    }

    @Test
    void followUserThrowsWhenSelfFollow() {
        assertThatThrownBy(() -> followService.followUser(1L, 1L))
                .isInstanceOf(SelfFollowException.class);
    }

    @Test
    void getFollowersReturnsSummaryList() {
        List<FollowEntity> follows = List.of(FollowEntity.builder().followerId(1L).followingId(3L).build());
        when(followRepository.findByFollowingId(3L)).thenReturn(follows);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(
                UserEntity.builder().id(1L).username("u1").firstName("User").lastName("One").build()));

        List<UserSummaryDto> result = followService.getFollowers(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("User One");
    }

    @Test
    void getFollowingReturnsEmptyWhenNoFollows() {
        when(followRepository.findByFollowerId(5L)).thenReturn(List.of());

        List<UserSummaryDto> result = followService.getFollowing(5L);

        assertThat(result).isEmpty();
    }

    @Test
    void isFollowingReturnsStatus() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        FollowResponseDto result = followService.isFollowing(1L, 2L);

        assertThat(result.isFollowing()).isTrue();
    }

    @Test
    void getCountsReturnValues() {
        when(followRepository.countByFollowingId(1L)).thenReturn(3L);
        when(followRepository.countByFollowerId(1L)).thenReturn(4L);

        FollowCountDto followers = followService.getFollowersCount(1L);
        FollowCountDto following = followService.getFollowingCount(1L);

        assertThat(followers.getCount()).isEqualTo(3);
        assertThat(following.getCount()).isEqualTo(4);
    }
}
