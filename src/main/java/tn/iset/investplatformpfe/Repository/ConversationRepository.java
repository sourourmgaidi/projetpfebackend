package tn.iset.investplatformpfe.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.investplatformpfe.Entity.Conversation;
import tn.iset.investplatformpfe.Entity.LocalPartner;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Pour INVESTISSEUR ou PARTENAIRE ECONOMIQUE : toutes ses conversations
    // Changé: expediteurEmail -> senderEmail, dateDernierMessage -> lastMessageDate
    List<Conversation> findBySenderEmailOrderByLastMessageDateDesc(String senderEmail);

    // Pour PARTENAIRE LOCAL : toutes ses conversations
    // Changé: dateDernierMessage -> lastMessageDate
    List<Conversation> findByPartnerOrderByLastMessageDateDesc(LocalPartner partner);

    // Trouver une conversation spécifique entre un expéditeur et un partenaire
    // Changé: expediteurEmail -> senderEmail
    Optional<Conversation> findBySenderEmailAndPartner(String senderEmail, LocalPartner partner);

    // Rechercher des conversations pour un expéditeur (par nom du partenaire)
    @Query("SELECT c FROM Conversation c WHERE c.senderEmail = :email AND " +  // Changé expediteurEmail -> senderEmail
            "(LOWER(c.partner.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.partner.firstName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Conversation> searchSenderConversations(@Param("email") String email, @Param("search") String search);  // Renommé

    // Optionnel: trouver toutes les conversations d'un partenaire (utile pour les notifications)
    List<Conversation> findByPartner(LocalPartner partner);

    // Optionnel: trouver les conversations non lues par un partenaire
    @Query("SELECT c FROM Conversation c WHERE c.partner = :partner AND c.partnerViewed = false")
    List<Conversation> findUnreadByPartner(@Param("partner") LocalPartner partner);

    // Optionnel: trouver les conversations non lues par un expéditeur
    @Query("SELECT c FROM Conversation c WHERE c.senderEmail = :email AND c.senderViewed = false")
    List<Conversation> findUnreadBySender(@Param("email") String email);
}