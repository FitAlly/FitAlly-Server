package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.dto.chat.request.ChatSendMessageRequest;
import com.fitally.backend.dto.chat.response.ChatMessageResponse;
import com.fitally.backend.dto.chat.response.ChatRoomResponse;
import com.fitally.backend.entity.ChatMessage;
import com.fitally.backend.entity.ChatParticipant;
import com.fitally.backend.entity.ChatParticipantId;
import com.fitally.backend.entity.ChatRoom;
import com.fitally.backend.entity.User;
import com.fitally.backend.repository.ChatMessageRepository;
import com.fitally.backend.repository.ChatParticipantRepository;
import com.fitally.backend.repository.ChatRoomRepository;
import com.fitally.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private final ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
    private final ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
    private final ChatParticipantRepository chatParticipantRepository = mock(ChatParticipantRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

    private final ChatService chatService = new ChatService(
            chatRoomRepository,
            chatMessageRepository,
            chatParticipantRepository,
            userRepository,
            messagingTemplate
    );

    @Test
    @DisplayName("sendMessage는 텍스트 메시지를 저장하고 채팅방 마지막 메시지와 안 읽은 수를 갱신한 뒤 브로드캐스트한다")
    void sendMessage_withTextMessage_savesAndBroadcasts() {
        // given
        Long currentUserId = 1L;
        Long opponentUserId = 2L;
        Long roomId = 100L;

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(roomId);
        request.setMessageText("안녕하세요");
        request.setImageUrl(null);

        ChatParticipant me = createParticipant(roomId, currentUserId, 0);
        ChatParticipant opponent = createParticipant(roomId, opponentUserId, 3);

        ChatRoom room = createRoom(roomId, "direct", null, null);

        User sender = createUser(currentUserId, "혁진", "https://image.test/me.png");

        ChatMessage savedMessage = createMessage(
                500L,
                roomId,
                currentUserId,
                "안녕하세요",
                null,
                LocalDateTime.of(2026, 4, 10, 12, 0)
        );

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.of(me));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatParticipantRepository.findByIdRoomId(roomId)).thenReturn(List.of(me, opponent));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sender));

        // when
        ChatMessageResponse response = chatService.sendMessage(currentUserId, request);

        // then
        assertNotNull(response);
        assertEquals(500L, response.getMessageId());
        assertEquals(roomId, response.getRoomId());
        assertEquals(currentUserId, response.getSenderId());
        assertEquals("혁진", response.getSenderNickname());
        assertEquals("안녕하세요", response.getMessageText());
        assertTrue(response.isMine());

        assertEquals("안녕하세요", room.getLastMessage());
        assertNotNull(room.getLastMessageAt());
        assertEquals(4, opponent.getUnreadCount());

        verify(chatParticipantRepository, times(1))
                .findByIdRoomIdAndIdUserId(roomId, currentUserId);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(chatRoomRepository, times(1)).findById(roomId);
        verify(chatParticipantRepository, times(1)).findByIdRoomId(roomId);
        verify(userRepository, times(1)).findById(currentUserId);
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/sub/chat.room." + roomId), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("sendMessage는 이미지 메시지일 때 마지막 메시지를 [이미지]로 저장한다")
    void sendMessage_withImageMessage_setsLastMessageAsImage() {
        // given
        Long currentUserId = 1L;
        Long opponentUserId = 2L;
        Long roomId = 200L;

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(roomId);
        request.setMessageText(null);
        request.setImageUrl("https://image.test/chat.png");

        ChatParticipant me = createParticipant(roomId, currentUserId, 0);
        ChatParticipant opponent = createParticipant(roomId, opponentUserId, 0);

        ChatRoom room = createRoom(roomId, "direct", null, null);

        User sender = createUser(currentUserId, "혁진", "https://image.test/me.png");

        ChatMessage savedMessage = createMessage(
                700L,
                roomId,
                currentUserId,
                null,
                "https://image.test/chat.png",
                LocalDateTime.of(2026, 4, 10, 13, 0)
        );

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.of(me));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatParticipantRepository.findByIdRoomId(roomId)).thenReturn(List.of(me, opponent));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sender));

        // when
        ChatMessageResponse response = chatService.sendMessage(currentUserId, request);

        // then
        assertNotNull(response);
        assertEquals("[이미지]", room.getLastMessage());
        assertEquals(1, opponent.getUnreadCount());

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/sub/chat.room." + roomId), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("sendMessage는 텍스트와 이미지가 모두 없으면 예외가 발생한다")
    void sendMessage_withoutTextAndImage_throwsException() {
        // given
        Long currentUserId = 1L;
        Long roomId = 300L;

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(roomId);
        request.setMessageText("   ");
        request.setImageUrl("   ");

        ChatParticipant me = createParticipant(roomId, currentUserId, 0);

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.of(me));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatService.sendMessage(currentUserId, request)
        );

        // then
        assertEquals("메시지 내용이 없습니다.", exception.getMessage());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(ChatMessageResponse.class));
    }

    @Test
    @DisplayName("sendMessage는 채팅방 참여자가 아니면 예외가 발생한다")
    void sendMessage_whenNotParticipant_throwsException() {
        // given
        Long currentUserId = 1L;
        Long roomId = 400L;

        ChatSendMessageRequest request = new ChatSendMessageRequest();
        request.setRoomId(roomId);
        request.setMessageText("테스트");

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatService.sendMessage(currentUserId, request)
        );

        // then
        assertEquals("해당 채팅방 참여자가 아닙니다.", exception.getMessage());
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("getChatRooms는 내 채팅방 목록을 상대방 정보와 함께 반환한다")
    void getChatRooms_returnsRoomListWithOpponentInfo() {
        // given
        Long currentUserId = 1L;
        Long opponentUserId = 2L;
        Long roomId = 10L;

        ChatParticipant myParticipant = createParticipant(roomId, currentUserId, 2);
        ChatParticipant opponentParticipant = createParticipant(roomId, opponentUserId, 0);

        ChatRoom room = createRoom(
                roomId,
                "direct",
                "주말에 운동 가능하세요?",
                LocalDateTime.of(2026, 4, 10, 9, 30)
        );

        User opponent = createUser(opponentUserId, "김운동", "https://image.test/opponent.png");

        when(chatParticipantRepository.findByIdUserId(currentUserId)).thenReturn(List.of(myParticipant));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(chatParticipantRepository.findByIdRoomId(roomId))
                .thenReturn(List.of(myParticipant, opponentParticipant));
        when(userRepository.findById(opponentUserId)).thenReturn(Optional.of(opponent));

        // when
        List<ChatRoomResponse> result = chatService.getChatRooms(currentUserId);

        // then
        assertEquals(1, result.size());

        ChatRoomResponse response = result.get(0);
        assertEquals(roomId, response.getRoomId());
        assertEquals(opponentUserId, response.getOpponentUserId());
        assertEquals("김운동", response.getOpponentNickname());
        assertEquals("주말에 운동 가능하세요?", response.getLastMessage());
        assertEquals(2, response.getUnreadCount());
    }

    @Test
    @DisplayName("getMessages는 메시지 페이지를 조회해 발신자 정보와 mine 여부를 포함해 반환한다")
    void getMessages_returnsPagedMessages() {
        // given
        Long currentUserId = 1L;
        Long roomId = 20L;

        ChatParticipant me = createParticipant(roomId, currentUserId, 0);

        ChatMessage message = createMessage(
                1000L,
                roomId,
                currentUserId,
                "안녕하세요",
                null,
                LocalDateTime.of(2026, 4, 10, 14, 0)
        );

        Page<ChatMessage> messagePage = new PageImpl<>(List.of(message), PageRequest.of(0, 20), 1);

        User sender = createUser(currentUserId, "혁진", "https://image.test/me.png");

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.of(me));
        when(chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(eq(roomId), any(Pageable.class)))
                .thenReturn(messagePage);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sender));

        // when
        Page<ChatMessageResponse> result = chatService.getMessages(currentUserId, roomId, 0, 20);

        // then
        assertEquals(1, result.getTotalElements());

        ChatMessageResponse response = result.getContent().get(0);
        assertEquals(1000L, response.getMessageId());
        assertEquals("혁진", response.getSenderNickname());
        assertEquals("안녕하세요", response.getMessageText());
        assertTrue(response.isMine());
    }

    @Test
    @DisplayName("createDirectRoom는 기존 1대1 채팅방이 있으면 새로 만들지 않고 기존 roomId를 반환한다")
    void createDirectRoom_returnsExistingRoomIdWhenAlreadyExists() {
        // given
        Long currentUserId = 1L;
        Long opponentUserId = 2L;
        Long roomId = 30L;

        ChatParticipant myRoom = createParticipant(roomId, currentUserId, 0);
        ChatParticipant me = createParticipant(roomId, currentUserId, 0);
        ChatParticipant opponent = createParticipant(roomId, opponentUserId, 0);

        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(createUser(currentUserId, "혁진", null)));
        when(userRepository.findById(opponentUserId))
                .thenReturn(Optional.of(createUser(opponentUserId, "상대", null)));
        when(chatParticipantRepository.findByIdUserId(currentUserId))
                .thenReturn(List.of(myRoom));
        when(chatParticipantRepository.findByIdRoomId(roomId))
                .thenReturn(List.of(me, opponent));

        // when
        Long result = chatService.createDirectRoom(currentUserId, opponentUserId);

        // then
        assertEquals(roomId, result);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
        verify(chatParticipantRepository, never()).save(any(ChatParticipant.class));
    }

    @Test
    @DisplayName("createDirectRoom는 기존 1대1 채팅방이 없으면 새 채팅방과 참여자를 저장한다")
    void createDirectRoom_createsNewRoomWhenNotExists() {
        // given
        Long currentUserId = 1L;
        Long opponentUserId = 2L;
        Long newRoomId = 999L;

        ChatRoom savedRoom = createRoom(newRoomId, "direct", null, null);

        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(createUser(currentUserId, "혁진", null)));
        when(userRepository.findById(opponentUserId))
                .thenReturn(Optional.of(createUser(opponentUserId, "상대", null)));
        when(chatParticipantRepository.findByIdUserId(currentUserId))
                .thenReturn(List.of());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(savedRoom);

        // when
        Long result = chatService.createDirectRoom(currentUserId, opponentUserId);

        // then
        assertEquals(newRoomId, result);
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(chatParticipantRepository, times(2)).save(any(ChatParticipant.class));
    }

    @Test
    @DisplayName("createDirectRoom는 자기 자신과의 채팅방 생성 시 예외가 발생한다")
    void createDirectRoom_withSameUser_throwsException() {
        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatService.createDirectRoom(1L, 1L)
        );

        // then
        assertEquals("자기 자신과는 채팅할 수 없습니다.", exception.getMessage());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("markAsRead는 내 unreadCount를 0으로 만든다")
    void markAsRead_setsUnreadCountToZero() {
        // given
        Long currentUserId = 1L;
        Long roomId = 55L;

        ChatParticipant participant = createParticipant(roomId, currentUserId, 7);

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.of(participant));

        // when
        chatService.markAsRead(currentUserId, roomId);

        // then
        assertEquals(0, participant.getUnreadCount());
    }

    @Test
    @DisplayName("markAsRead는 참여자가 아니면 예외가 발생한다")
    void markAsRead_whenParticipantNotFound_throwsException() {
        // given
        Long currentUserId = 1L;
        Long roomId = 56L;

        when(chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, currentUserId))
                .thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> chatService.markAsRead(currentUserId, roomId)
        );

        // then
        assertEquals("해당 채팅방 참여자가 아닙니다.", exception.getMessage());
    }

    private ChatParticipant createParticipant(Long roomId, Long userId, Integer unreadCount) {
        ChatParticipant participant = new ChatParticipant();
        participant.setId(new ChatParticipantId(roomId, userId));
        participant.setUnreadCount(unreadCount);
        return participant;
    }

    private ChatRoom createRoom(Long roomId, String roomType, String lastMessage, LocalDateTime lastMessageAt) {
        ChatRoom room = new ChatRoom();

        setField(room, "roomId", roomId);
        room.setRoomType(roomType);
        room.setLastMessage(lastMessage);
        room.setLastMessageAt(lastMessageAt);

        return room;
    }

    private ChatMessage createMessage(Long messageId,
                                      Long roomId,
                                      Long senderId,
                                      String messageText,
                                      String imageUrl,
                                      LocalDateTime createdAt) {
        ChatMessage message = new ChatMessage();

        setField(message, "messageId", messageId);
        message.setRoomId(roomId);
        message.setSenderId(senderId);
        message.setMessageText(messageText);
        message.setImageUrl(imageUrl);
        setField(message, "createdAt", createdAt);

        return message;
    }

    private User createUser(Long userId, String nickname, String profileImageUrl) {
        User user = User.builder()
                .userId(userId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
        return user;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}