package com.koliving.api.auth.login;

import com.koliving.api.user.domain.User;
import com.koliving.api.user.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.koliving.api.user.UserUtils.createAuthentication;
import static com.koliving.api.user.UserUtils.createUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginProviderTest {

    @Mock
    UserService userService;

    @InjectMocks
    LoginProvider loginProvider;

    @Test
    @DisplayName("authenticate() 성공 : 로그인 인증")
    void authenticate_success() {
        String dummyEmail = "test@koliving.com";
        String dummyPassword = "KolivingPwd12";
        User dummyUser = createUser(dummyEmail, dummyPassword);

        when(userService.loadUserByUsername(dummyEmail)).thenReturn(dummyUser);
        when(userService.isEqualPassword(dummyPassword, dummyUser.getPassword())).thenReturn(true);

        Authentication auth = createAuthentication(dummyEmail, dummyPassword);
        Authentication authentication = loginProvider.authenticate(auth);

        assertNotNull(authentication);
        assertEquals(dummyEmail, authentication.getName());
    }

    @Test
    @DisplayName("authenticate() 실패 : 존재하지 않는 이메일")
    void authenticate_failure_non_exists_email() {
        String nonExistsEmail = "noAtMark";
        String dummyPassword = "KolivingPwd12";

        when(userService.loadUserByUsername(nonExistsEmail)).thenThrow(UsernameNotFoundException.class);
        Authentication auth = createAuthentication(nonExistsEmail, dummyPassword);

        assertThrows(UsernameNotFoundException.class, () -> loginProvider.authenticate(auth));
    }

    @Test
    @DisplayName("authenticate() 실패 : 일치하지 않는 패스워드")
    void authenticate_failure_mismatched_password() {
        String dummyEmail = "test@koliving.com";
        String dummyPassword = "KolivingPwd12";
        User dummyUser = createUser(dummyEmail, dummyPassword);

        when(userService.loadUserByUsername(dummyEmail)).thenReturn(dummyUser);

        String mismatchedPassword = "not_equal_password";
        when(userService.isEqualPassword(mismatchedPassword, dummyUser.getPassword())).thenReturn(false);

        Authentication auth = createAuthentication(dummyEmail, mismatchedPassword);

        assertThrows(BadCredentialsException.class, () -> loginProvider.authenticate(auth));
    }
}
