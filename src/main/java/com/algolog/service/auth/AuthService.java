package com.algolog.service.auth;

import com.algolog.domain.user.User;
import com.algolog.dto.auth.LoginRequest;
import com.algolog.dto.auth.LoginResponse;
import com.algolog.dto.auth.SignupRequest;
import com.algolog.dto.auth.SignupResponse;
import com.algolog.global.exception.BusinessException;
import com.algolog.global.exception.ErrorCode;
import com.algolog.global.security.jwt.JwtTokenProvider;
import com.algolog.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicateEmail(request.email());

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .nickname(request.nickname())
            .build();

        User savedUser = userRepository.save(user);
        return SignupResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        return LoginResponse.bearer(accessToken);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
