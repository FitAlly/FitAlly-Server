package com.fitally.backend.service;

import com.fitally.backend.common.exception.BusinessException;
import com.fitally.backend.common.exception.ErrorCode;
import com.fitally.backend.dto.chat.request.ChatSendMessageRequest;
import com.fitally.backend.dto.chat.response.ChatMessageResponse;
import com.fitally.backend.dto.chat.response.ChatReadResponse;
import com.fitally.backend.dto.chat.response.ChatRoomResponse;
import com.fitally.backend.entity.*;
import com.fitally.backend.repository.ChatMessageRepository;
import com.fitally.backend.repository.ChatParticipantRepository;
import com.fitally.backend.repository.ChatRoomRepository;
import com.fitally.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRooms(Long currentUserId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserIdOrderByRecent(currentUserId);
        List<ChatRoomResponse> result = new ArrayList<>();

        for (ChatRoom room : chatRooms) {
            Long roomId = room.getRoomId();

            List<ChatParticipant> participants = chatParticipantRepository.findByIdRoomId(roomId);

            ChatParticipant myParticipant = participants.stream()
                    .filter(p -> p.getId().getUserId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            Long opponentUserId = participants.stream()
                    .map(p -> p.getId().getUserId())
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            String opponentNickName = null;
            String opponentProfileImageUrl = null;

            if (opponentUserId != null) {
                User opponent = userRepository.findById(opponentUserId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_OPPONENT_NOT_FOUND));

                opponentNickName = opponent.getNickname();
                opponentProfileImageUrl = opponent.getProfileImageUrl();
            }

            result.add(new ChatRoomResponse(
                    room.getRoomId(),
                    opponentUserId,
                    opponentNickName,
                    opponentProfileImageUrl,
                    room.getLastMessage(),
                    room.getLastMessageAt(),
                    myParticipant.getUnreadCount() == null ? 0 : myParticipant.getUnreadCount()
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long currentUserId, Long roomId, int page, int size) {
        validateParticipant(roomId, currentUserId);

        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(page, size))
                .map(message -> {
                    User sender = userRepository.findById(message.getSenderId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_SENDER_NOT_FOUND));

                    return new ChatMessageResponse(
                            message.getMessageId(),
                            message.getRoomId(),
                            message.getSenderId(),
                            sender.getNickname(),
                            sender.getProfileImageUrl(),
                            message.getMessageText(),
                            message.getImageUrl(),
                            message.getCreatedAt(),
                            message.getSenderId().equals(currentUserId)
                    );
                });
    }

    public ChatMessageResponse sendMessage(Long currentUserId, ChatSendMessageRequest request) {
        validateParticipant(request.getRoomId(), currentUserId);

        boolean hasText = request.getMessageText() != null && !request.getMessageText().trim().isEmpty();
        boolean hasImage = request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty();

        if (!hasText && !hasImage) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_EMPTY);
        }

        ChatMessage message = new ChatMessage();
        message.setRoomId(request.getRoomId());
        message.setSenderId(currentUserId);
        message.setMessageText(hasText ? request.getMessageText().trim() : null);
        message.setImageUrl(hasImage ? request.getImageUrl().trim() : null);

        ChatMessage saved = chatMessageRepository.save(message);

        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        room.setLastMessage(hasText ? request.getMessageText().trim() : "[이미지]");
        room.setLastMessageAt(LocalDateTime.now());

        List<ChatParticipant> participants = chatParticipantRepository.findByIdRoomId(request.getRoomId());
        for (ChatParticipant participant : participants) {
            if (!participant.getId().getUserId().equals(currentUserId)) {
                int unread = participant.getUnreadCount() == null ? 0 : participant.getUnreadCount();
                participant.setUnreadCount(unread + 1);
            }
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_USER_NOT_FOUND));

        ChatMessageResponse response = new ChatMessageResponse(
                saved.getMessageId(),
                saved.getRoomId(),
                saved.getSenderId(),
                sender.getNickname(),
                sender.getProfileImageUrl(),
                saved.getMessageText(),
                saved.getImageUrl(),
                saved.getCreatedAt(),
                true
        );

        messagingTemplate.convertAndSend("/sub/chat.room." + request.getRoomId(), response);

        return response;
    }

    public Long createDirectRoom(Long currentUserId, Long opponentUserId) {
        if (currentUserId.equals(opponentUserId)) {
            throw new BusinessException(ErrorCode.CHAT_SELF_ROOM_NOT_ALLOWED);
        }

        userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_USER_NOT_FOUND));
        userRepository.findById(opponentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_OPPONENT_NOT_FOUND));

        List<ChatParticipant> myRooms = chatParticipantRepository.findByIdUserId(currentUserId);

        for (ChatParticipant myRoom : myRooms) {
            List<ChatParticipant> participants = chatParticipantRepository.findByIdRoomId(myRoom.getId().getRoomId());

            boolean containsOpponent = participants.stream()
                    .anyMatch(p -> p.getId().getUserId().equals(opponentUserId));

            if (containsOpponent && participants.size() == 2) {
                return myRoom.getId().getRoomId();
            }
        }

        ChatRoom room = new ChatRoom();
        room.setRoomType("direct");
        ChatRoom savedRoom = chatRoomRepository.save(room);

        ChatParticipant me = new ChatParticipant();
        me.setId(new ChatParticipantId(savedRoom.getRoomId(), currentUserId));

        ChatParticipant opponent = new ChatParticipant();
        opponent.setId(new ChatParticipantId(savedRoom.getRoomId(), opponentUserId));

        chatParticipantRepository.save(me);
        chatParticipantRepository.save(opponent);

        return savedRoom.getRoomId();
    }

    public void markAsRead(Long currentUserId, Long roomId) {
        ChatParticipant participant = chatParticipantRepository
                .findByIdRoomIdAndIdUserId(roomId, currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
        participant.setUnreadCount(0);

        participant.setUnreadCount(0);

        ChatReadResponse response = new ChatReadResponse(
                roomId,
                currentUserId,
                0,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/sub/chat.room." + roomId + ".read",
                response
        );
    }

    private void validateParticipant(Long roomId, Long userId) {
        chatParticipantRepository.findByIdRoomIdAndIdUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND));
    }
}
