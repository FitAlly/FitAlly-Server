package com.fitally.backend.repository;

import com.fitally.backend.entity.ChatParticipant;
import com.fitally.backend.entity.ChatParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {

    List<ChatParticipant> findByIdUserId(Long userId);

    List<ChatParticipant> findByIdRoomId(Long roomId);

    Optional<ChatParticipant> findByIdRoomIdAndUserId(Long roomId, Long userId);
}
