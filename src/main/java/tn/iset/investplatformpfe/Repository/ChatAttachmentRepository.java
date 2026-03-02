package tn.iset.investplatformpfe.Repository;

import tn.iset.investplatformpfe.Entity.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {

    List<ChatAttachment> findByMessageMessageId(String messageId);

    @Query("SELECT a FROM ChatAttachment a WHERE a.message.chat.chatId = :chatId ORDER BY a.uploadedAt ASC")
    List<ChatAttachment> findByChatId(@Param("chatId") String chatId);

    @Query("SELECT a FROM ChatAttachment a WHERE a.message.chat.chatId = :chatId AND a.fileType LIKE 'image/%'")
    List<ChatAttachment> findImagesByChatId(@Param("chatId") String chatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatAttachment a WHERE a.message.messageId = :messageId")
    void deleteByMessageId(@Param("messageId") String messageId);
    @Query("SELECT COUNT(a) FROM ChatAttachment a WHERE a.message.chat.chatId = :chatId")
    long countByChatId(@Param("chatId") String chatId);

    @Query("SELECT COUNT(a) FROM ChatAttachment a WHERE a.message.chat.chatId = :chatId AND a.fileType LIKE 'image/%'")
    long countImagesByChatId(@Param("chatId") String chatId);

}