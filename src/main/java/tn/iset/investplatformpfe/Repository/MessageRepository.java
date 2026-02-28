package tn.iset.investplatformpfe.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.investplatformpfe.Entity.Conversation;
import tn.iset.investplatformpfe.Entity.Message;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Trouver les messages d'une conversation par ordre chronologique
    List<Message> findByConversationOrderBySentDateAsc(Conversation conversation);

    // Trouver les messages non lus par destinataire
    List<Message> findByRecipientEmailAndReadFalse(String recipientEmail);

    // Marquer les messages comme lus
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.recipientEmail = :recipient AND m.conversation = :conversation")
    void markMessagesAsRead(@Param("recipient") String recipient, @Param("conversation") Conversation conversation);

    // Compter les messages non lus par destinataire
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientEmail = :email AND m.read = false")
    long countUnreadByRecipient(@Param("email") String email);
}