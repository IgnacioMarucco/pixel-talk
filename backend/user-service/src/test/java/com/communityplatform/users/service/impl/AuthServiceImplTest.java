package com.communityplatform.users.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.communityplatform.users.config.JwtProperties;
import com.communityplatform.users.dto.auth.AuthResponseDto;
import com.communityplatform.users.dto.auth.LoginRequestDto;
import com.communityplatform.users.dto.auth.RefreshTokenRequestDto;
import com.communityplatform.users.dto.auth.RegisterRequestDto;
import com.communityplatform.users.entity.RefreshTokenEntity;
import com.communityplatform.users.entity.RoleEntity;
import com.communityplatform.users.entity.UserEntity;
import com.communityplatform.users.enums.RoleName;
import com.communityplatform.users.exception.DuplicateUserException;
import com.communityplatform.users.repository.RefreshTokenRepository;
import com.communityplatform.users.repository.RoleRepository;
import com.communityplatform.users.repository.UserRepository;
import com.communityplatform.users.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private JwtProperties jwtProperties;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        authService = new AuthServiceImpl(userRepository, roleRepository, refreshTokenRepository,
                passwordEncoder, authenticationManager, jwtTokenProvider, jwtProperties);
    }

    @Test
    void registerCreatesUserAndTokens() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("user1")
                .email("user1@example.com")
                .password("Password1!")
                .firstName("User")
                .lastName("One")
                .build();

        RoleEntity role = RoleEntity.builder().roleName(RoleName.ROLE_USER).build();
        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .roles(Set.of(role))
                .build();

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(1L, "user1")).thenReturn("access");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .token("refresh")
                .user(savedUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenReturn(refreshToken);

        AuthResponseDto response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getUsername()).isEqualTo("user1");
    }

    @Test
    void registerThrowsWhenUsernameExists() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("user1")
                .email("user1@example.com")
                .password("Password1!")
                .build();

        when(userRepository.existsByUsername("user1")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void loginAuthenticatesAndReturnsTokens() {
        LoginRequestDto request = LoginRequestDto.builder()
                .usernameOrEmail("user1")
                .password("Password1!")
                .build();

        RoleEntity role = RoleEntity.builder().roleName(RoleName.ROLE_USER).build();
        UserEntity user = UserEntity.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .roles(Set.of(role))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken("user1", "Password1!");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail("user1", "user1")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, "user1")).thenReturn("access");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .token("refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenReturn(refreshToken);

        AuthResponseDto response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(refreshTokenRepository).revokeAllByUser(user);
    }

    @Test
    void refreshTokenRotatesTokens() {
        UserEntity user = UserEntity.builder().id(2L).username("user2").email("user2@example.com").build();
        RefreshTokenEntity existingToken = RefreshTokenEntity.builder()
                .token("refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("refresh")).thenReturn(Optional.of(existingToken));
        when(jwtTokenProvider.generateAccessToken(2L, "user2")).thenReturn("access");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                .token("refresh2")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenReturn(newToken);

        AuthResponseDto response = authService.refreshToken(RefreshTokenRequestDto.builder()
                .refreshToken("refresh")
                .build());

        assertThat(response.getRefreshToken()).isEqualTo("refresh2");
        assertThat(existingToken.getRevoked()).isTrue();
    }

    @Test
    void logoutRevokesToken() {
        UserEntity user = UserEntity.builder().id(3L).username("user3").email("user3@example.com").build();
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("refresh")).thenReturn(Optional.of(token));

        authService.logout("refresh");

        assertThat(token.getRevoked()).isTrue();
        verify(refreshTokenRepository).save(eq(token));
    }
}