package tn.iset.investplatformpfe.Repository;

import tn.iset.investplatformpfe.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // ========================================
    // FIND METHODS
    // ========================================

    List<ChatMessage> findByChatChatIdOrderByTimestampAsc(String chatId);

    Optional<ChatMessage> findByMessageId(String messageId);

    @Query("SELECT m FROM ChatMessage m WHERE m.chat.chatId = :chatId ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestByChatId(@Param("chatId") String chatId);

    // ========================================
    // READ STATUS
    // ========================================

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.chat.chatId = :chatId AND m.receiverId = :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("chatId") String chatId, @Param("userId") String userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chat.chatId = :chatId AND m.receiverId = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("chatId") String chatId, @Param("userId") String userId);

    // ========================================
    // COUNT METHODS
    // ========================================

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chat.chatId = :chatId")
    long countByChatId(@Param("chatId") String chatId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.senderId = :userId")
    long countSentByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :userId")
    long countReceivedByUser(@Param("userId") String userId);

    // ========================================
    // TIME METHODS
    // ========================================

    @Query("SELECT MAX(m.timestamp) FROM ChatMessage m WHERE m.chat.chatId = :chatId")
    LocalDateTime getLastMessageTime(@Param("chatId") String chatId);

    @Query("SELECT MIN(m.timestamp) FROM ChatMessage m WHERE m.chat.chatId = :chatId")
    LocalDateTime getFirstMessageTime(@Param("chatId") String chatId);

    // ========================================
    // SEARCH METHODS
    // ========================================

    @Query("SELECT m FROM ChatMessage m WHERE m.chat.chatId = :chatId AND m.content LIKE %:keyword% ORDER BY m.timestamp DESC")
    List<ChatMessage> searchInConversation(@Param("chatId") String chatId, @Param("keyword") String keyword);

    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :userId OR m.receiverId = :userId) AND m.content LIKE %:keyword% ORDER BY m.timestamp DESC")
    List<ChatMessage> searchAllUserMessages(@Param("userId") String userId, @Param("keyword") String keyword);

    // ========================================
    // DELETE METHODS
    // ========================================

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage m WHERE m.chat.chatId = :chatId")
    void deleteByChatId(@Param("chatId") String chatId);

    // ========================================
    // STATS METHODS
    // ========================================

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chat.chatId = :chatId AND m.type = :type")
    long countByType(@Param("chatId") String chatId, @Param("type") String type);

    @Query("SELECT DATE(m.timestamp), COUNT(m) FROM ChatMessage m WHERE m.chat.chatId = :chatId GROUP BY DATE(m.timestamp) ORDER BY DATE(m.timestamp)")
    List<Object[]> getMessageActivityByDay(@Param("chatId") String chatId);
}