package com.fitally.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitally.backend.common.enums.Role;
import com.fitally.backend.dto.chat.request.ChatSendMessageRequest;
import com.fitally.backend.dto.chat.response.ChatMessageResponse;
import com.fitally.backend.entity.ChatMessage;
import com.fitally.backend.entity.ChatParticipant;
import com.fitally.backend.entity.ChatParticipantId;
import com.fitally.backend.entity.ChatRoom;
import com.fitally.backend.entity.User;
import com.fitally.backend.repository.ChatMessageRepository;
import com.fitally.backend.repository.ChatParticipantRepository;
import com.fitally.backend.repository.ChatRoomRepository;
import com.fitally.backend.repository.UserRepository;
import com.fitally.backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketChatIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @AfterEach
    void tearDown() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    @Test
    @DisplayName("JWT 인증으로 WebSocket 연결 후 채팅방 구독 상태에서 메시지를 보내면 실시간 수신되고 DB에도 저장된다")
    void websocketChat_withJwt_connectSubscribeSendAndReceive() throws Exception {
        // given
        User sender = saveUser(
                "sender_" + System.nanoTime() + "@test.com",
                "보내는사람" + System.nanoTime());
        User receiver = saveUser("receiver_" + System.nanoTime() + "@test.com",
                "받는사람" + System.nanoTime());

        ChatRoom room = saveChatRoom("direct");
        saveParticipant(room.getRoomId(), sender.getUserId(), 0);
        saveParticipant(room.getRoomId(), receiver.getUserId(), 0);

        String accessToken = jwtTokenProvider.createAccessToken(sender);

        stompClient = createStompClient();

        String url = "ws://localhost:" + port + "/ws-chat";

        BlockingQueue<ChatMessageResponse> blockingQueue = new LinkedBlockingQueue<>();

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + accessToken);

        // when
        stompSession = stompClient.connectAsync(
                url,
                handshakeHeaders,
                connectHeaders,
                new TestSessionHandler()
        ).get(5, TimeUnit.SECONDS);

        assertNotNull(stompSession);
        assertTrue(stompSession.isConnected());

        stompSession.subscribe("/sub/chat.room." + room.getRoomId(), new StompFrameHandler() {
            @Override
            public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.offer((ChatMessageResponse) payload);
            }
        });

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(room.getRoomId());
        request.setMessageText("실시간 통합 테스트 메시지");
        request.setImageUrl(null);

        stompSession.send("/pub/chat.send", request);

        ChatMessageResponse received = blockingQueue.poll(5, TimeUnit.SECONDS);

        // then
        assertNotNull(received, "실시간 메시지를 수신하지 못했습니다.");
        assertEquals(room.getRoomId(), received.getRoomId());
        assertEquals(sender.getUserId(), received.getSenderId());
        assertEquals("실시간 통합 테스트 메시지", received.getMessageText());
        assertTrue(received.isMine());

        Page<ChatMessage> messagePage = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(room.getRoomId(), PageRequest.of(0,10));
        assertFalse(messagePage.isEmpty(), "DB에 메시지가 저장되지 않았습니다.");

        ChatMessage saved = messagePage.getContent().get(0);
        assertEquals(room.getRoomId(), saved.getRoomId());
        assertEquals(sender.getUserId(), saved.getSenderId());
        assertEquals("실시간 통합 테스트 메시지", saved.getMessageText());

        ChatRoom updatedRoom = chatRoomRepository.findById(room.getRoomId()).orElseThrow();
        assertEquals("실시간 통합 테스트 메시지", updatedRoom.getLastMessage());
        assertNotNull(updatedRoom.getLastMessageAt());
    }

    private WebSocketStompClient createStompClient() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);

        client.setMessageConverter(converter);
        client.setInboundMessageSizeLimit(64 * 1024);
        return client;
    }

    private User saveUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .passwordHash("dummy-password")
                .provider("email")
                .role(Role.USER)
                .birthdate(LocalDate.of(1995, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private ChatRoom saveChatRoom(String roomType) {
        ChatRoom room = new ChatRoom();
        room.setRoomType(roomType);
        room.setLastMessage(null);
        room.setLastMessageAt(null);
        return chatRoomRepository.save(room);
    }

    private ChatParticipant saveParticipant(Long roomId, Long userId, Integer unreadCount) {
        ChatParticipant participant = new ChatParticipant();
        participant.setId(new ChatParticipantId(roomId, userId));
        participant.setUnreadCount(unreadCount);
        return chatParticipantRepository.save(participant);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class TestSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
        }

        @Override
        public void handleException(StompSession session,
                                    StompCommand command,
                                    StompHeaders headers,
                                    byte[] payload,
                                    Throwable exception) {
            throw new RuntimeException(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            throw new RuntimeException(exception);
        }
    }
}