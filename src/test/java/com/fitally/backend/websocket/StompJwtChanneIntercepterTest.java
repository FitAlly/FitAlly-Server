package com.fitally.backend.websocket;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StompJwtChannelInterceptorTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final StompJwtChannelInterceptor interceptor =
            new StompJwtChannelInterceptor(jwtTokenProvider);

    @Test
    @DisplayName("CONNECT 이고 JWT가 정상이면 사용자 인증정보가 저장된다")
    void preSend_connectWithValidToken_setsAuthenticationUser() {
        // given
        String token = "valid-jwt-token";
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList());

        Message<?> message = createStompMessage(
                StompCommand.CONNECT,
                "Bearer " + token
        );

        doNothing().when(jwtTokenProvider).validateToken(token);
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(authentication);

        // when
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        // then
        assertNotNull(result);

        Object userHeader = result.getHeaders().get("simpUser");
        assertNotNull(userHeader);
        assertEquals(authentication, userHeader);

        verify(jwtTokenProvider, times(1)).validateToken(token);
        verify(jwtTokenProvider, times(1)).getAuthentication(token);
    }

    @Test
    @DisplayName("CONNECT 인데 Authorization 헤더가 없으면 예외가 발생한다")
    void preSend_connectWithoutAuthorization_throwsException() {
        // given
        Message<?> message = createStompMessage(
                StompCommand.CONNECT,
                null
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> interceptor.preSend(message, mock(MessageChannel.class))
        );

        // then
        assertNotNull(exception);
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    @DisplayName("CONNECT 인데 Bearer 형식이 아니면 예외가 발생한다")
    void preSend_connectWithInvalidAuthorizationPrefix_throwsException() {
        // given
        Message<?> message = createStompMessage(
                StompCommand.CONNECT,
                "Basic abcdefg"
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> interceptor.preSend(message, mock(MessageChannel.class))
        );

        // then
        assertNotNull(exception);
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    @DisplayName("CONNECT 가 아닌 프레임이면 JWT 검증 없이 그대로 통과한다")
    void preSend_nonConnectCommand_passesWithoutJwtValidation() {
        // given
        Message<?> message = createStompMessage(
                StompCommand.SEND,
                null
        );

        // when
        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        // then
        assertNotNull(result);
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    @DisplayName("CONNECT 이고 Bearer 뒤에 토큰이 비어 있으면 예외가 발생한다")
    void preSend_connectWithBlankToken_throwsException() {
        // given
        Message<?> message = createStompMessage(
                StompCommand.CONNECT,
                "Bearer "
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> interceptor.preSend(message, mock(MessageChannel.class))
        );

        // then
        assertNotNull(exception);
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    @DisplayName("createStompMessage는 Authorization 헤더가 있으면 메시지에 정상적으로 담는다")
    void createStompMessage_withAuthorizationHeader_setsCommandAndHeader() {
        // given
        String authorizationHeader = "Bearer test-token";

        // when
        Message<?> message = createStompMessage(StompCommand.CONNECT, authorizationHeader);

        // then
        assertNotNull(message);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        assertEquals(StompCommand.CONNECT, accessor.getCommand());
        assertEquals(authorizationHeader, accessor.getFirstNativeHeader("Authorization"));
    }

    @Test
    @DisplayName("createStompMessage는 Authorization 헤더가 없으면 헤더 없이 메시지를 만든다")
    void createStompMessage_withoutAuthorizationHeader_createsMessageWithoutHeader() {
        // when
        Message<?> message = createStompMessage(StompCommand.SEND, null);

        // then
        assertNotNull(message);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        assertEquals(StompCommand.SEND, accessor.getCommand());
        assertNull(accessor.getFirstNativeHeader("Authorization"));
    }

    private Message<?> createStompMessage(StompCommand command, String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);

        if (authorizationHeader != null) {
            accessor.addNativeHeader("Authorization", authorizationHeader);
        }

        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}