package com.communityplatform.users.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.communityplatform.users.dto.user.ChangePasswordDto;
import com.communityplatform.users.dto.user.UserCreateDto;
import com.communityplatform.users.dto.user.UserResponseDto;
import com.communityplatform.users.dto.user.UserUpdateDto;
import com.communityplatform.users.entity.UserEntity;
import com.communityplatform.users.exception.BadCredentialsException;
import com.communityplatform.users.exception.DuplicateUserException;
import com.communityplatform.users.exception.UserNotFoundException;
import com.communityplatform.users.mapper.UserMapper;
import com.communityplatform.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserReturnsResponse() {
        UserCreateDto request = UserCreateDto.builder()
                .username("user1")
                .email("user1@example.com")
                .password("Password1!")
                .build();

        UserEntity entity = UserEntity.builder()
                .username("user1")
                .email("user1@example.com")
                .password("raw")
                .build();

        UserEntity saved = UserEntity.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .password("hashed")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .build();

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);
        when(userMapper.toResponseDto(saved)).thenReturn(response);

        UserResponseDto result = userService.createUser(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUserThrowsDuplicate() {
        UserCreateDto request = UserCreateDto.builder()
                .username("user1")
                .email("user1@example.com")
                .password("Password1!")
                .build();

        when(userRepository.existsByUsername("user1")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void getUserByIdReturnsResponse() {
        UserEntity entity = UserEntity.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();

        when(userRepository.findByIdWithRoles(2L)).thenReturn(Optional.of(entity));
        when(userMapper.toResponseDto(entity)).thenReturn(response);

        UserResponseDto result = userService.getUserById(2L);

        assertThat(result.getUsername()).isEqualTo("user2");
    }

    @Test
    void getUserByIdThrowsWhenDeleted() {
        UserEntity entity = UserEntity.builder()
                .id(3L)
                .username("user3")
                .email("user3@example.com")
                .build();
        entity.setDeletedAt(LocalDateTime.now());

        when(userRepository.findByIdWithRoles(3L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> userService.getUserById(3L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsersReturnsList() {
        List<UserEntity> entities = List.of(
                UserEntity.builder().id(1L).username("u1").email("u1@example.com").build());
        List<UserResponseDto> responses = List.of(
                UserResponseDto.builder().id(1L).username("u1").email("u1@example.com").build());

        when(userRepository.findAllByDeletedAtIsNull()).thenReturn(entities);
        when(userMapper.toResponseDtoList(entities)).thenReturn(responses);

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
    }

    @Test
    void updateUserReturnsResponse() {
        UserEntity entity = UserEntity.builder()
                .id(4L)
                .username("user4")
                .email("user4@example.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .firstName("New")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(4L)
                .username("user4")
                .email("user4@example.com")
                .firstName("New")
                .build();

        when(userRepository.findByIdWithRoles(4L)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(response);

        UserResponseDto result = userService.updateUser(4L, updateDto);

        assertThat(result.getFirstName()).isEqualTo("New");
        verify(userMapper).updateEntity(eq(entity), eq(updateDto));
    }

    @Test
    void deleteUserSoftDeletes() {
        UserEntity entity = UserEntity.builder()
                .id(5L)
                .username("user5")
                .email("user5@example.com")
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(entity));

        userService.deleteUser(5L);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void changePasswordThrowsForInvalidCurrentPassword() {
        UserEntity entity = UserEntity.builder()
                .id(6L)
                .username("user6")
                .email("user6@example.com")
                .password("hashed")
                .build();

        when(userRepository.findByIdWithRoles(6L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ChangePasswordDto dto = ChangePasswordDto.builder()
                .currentPassword("wrong")
                .newPassword("Password2!")
                .build();

        assertThatThrownBy(() -> userService.changePassword(6L, dto))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void updateCurrentUserReturnsResponse() {
        UserEntity entity = UserEntity.builder()
                .id(7L)
                .username("user7")
                .email("user7@example.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .firstName("New")
                .lastName("Name")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(7L)
                .username("user7")
                .email("user7@example.com")
                .firstName("New")
                .lastName("Name")
                .build();

        when(userRepository.findByUsernameWithRoles("user7")).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(response);

        UserResponseDto result = userService.updateCurrentUser("user7", updateDto);

        assertThat(result.getLastName()).isEqualTo("Name");
    }

    @Test
    void getCurrentUserReturnsResponse() {
        UserEntity entity = UserEntity.builder()
                .id(8L)
                .username("user8")
                .email("user8@example.com")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(8L)
                .username("user8")
                .email("user8@example.com")
                .build();

        when(userRepository.findByUsernameWithRoles("user8")).thenReturn(Optional.of(entity));
        when(userMapper.toResponseDto(entity)).thenReturn(response);

        UserResponseDto result = userService.getCurrentUser("user8");

        assertThat(result.getEmail()).isEqualTo("user8@example.com");
    }
}