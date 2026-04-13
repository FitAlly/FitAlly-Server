package com.fitally.backend.repository;

import com.fitally.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            select cr
            from ChatRoom cr
            where cr.roomId in (
                select cp.id.roomId
                from ChatParticipant cp
                where cp.id.userId = :userId
            )
            order by coalesce(cr.lastMessageAt, cr.createdAt) desc
            """)
    List<ChatRoom> findChatRoomsByUserIdOrderByRecent(Long userId);
}
