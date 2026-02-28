package tn.iset.investplatformpfe.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iset.investplatformpfe.Entity.*;
import tn.iset.investplatformpfe.Repository.*;
import tn.iset.investplatformpfe.Repository.EconomicPartnerRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessagerieService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final InvestorRepository investorRepo;
    private final EconomicPartnerRepository partenaireEcoRepo;
    private final LocalPartnerRepository localPartnerRepo;

    public MessagerieService(MessageRepository messageRepo,
                             ConversationRepository conversationRepo,
                             InvestorRepository investorRepo,
                             EconomicPartnerRepository partenaireEcoRepo,
                             LocalPartnerRepository localPartnerRepo) {
        this.messageRepo = messageRepo;
        this.conversationRepo = conversationRepo;
        this.investorRepo = investorRepo;
        this.partenaireEcoRepo = partenaireEcoRepo;
        this.localPartnerRepo = localPartnerRepo;
    }

    /**
     * Rechercher des partenaires locaux (pour investisseur ou partenaire économique)
     */
    public List<Map<String, Object>> searchLocalPartners(String search) {
        return localPartnerRepo.searchPartners(search).stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("fullName", p.getFirstName() + " " + p.getLastName());
                    map.put("email", p.getEmail());
                    map.put("domain", p.getActivityDomain());
                    return map;
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher dans les conversations d'un expéditeur (investisseur ou partenaire éco)
     */
    public List<Conversation> searchSenderConversations(String email, String search) {
        return conversationRepo.searchSenderConversations(email, search);
    }

    /**
     * Envoyer un message (crée la conversation si elle n'existe pas)
     */
    @Transactional
    public Message sendMessage(String senderEmail, String partnerEmail, String content, String role) {

        // Récupérer le partenaire local
        LocalPartner partner = localPartnerRepo.findByEmail(partnerEmail)
                .orElseThrow(() -> new RuntimeException("Local partner not found"));

        // Chercher la conversation existante OU en créer une nouvelle
        Conversation conversation = conversationRepo
                .findBySenderEmailAndPartner(senderEmail, partner)
                .orElseGet(() -> {
                    Conversation nouvelle = new Conversation(role, senderEmail, partner);
                    return conversationRepo.save(nouvelle);
                });

        // Créer et sauvegarder le message
        Message message = new Message(content, senderEmail, partnerEmail, conversation);
        message = messageRepo.save(message);

        // Mettre à jour la conversation avec les nouveaux noms anglais
        conversation.setLastMessage(content);
        conversation.setLastMessageDate(LocalDateTime.now());
        conversation.setSenderViewed(true);
        conversation.setPartnerViewed(false);
        conversationRepo.save(conversation);

        return message;
    }

    /**
     * Partenaire local répond à un expéditeur
     */
    @Transactional
    public Message replyMessage(String partnerEmail, String senderEmail, String content) {

        LocalPartner partner = localPartnerRepo.findByEmail(partnerEmail)
                .orElseThrow(() -> new RuntimeException("Local partner not found"));

        Conversation conversation = conversationRepo
                .findBySenderEmailAndPartner(senderEmail, partner)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = new Message(content, partnerEmail, senderEmail, conversation);
        message = messageRepo.save(message);

        // Mettre à jour la conversation avec les nouveaux noms anglais
        conversation.setLastMessage(content);
        conversation.setLastMessageDate(LocalDateTime.now());
        conversation.setSenderViewed(false);
        conversation.setPartnerViewed(true);
        conversationRepo.save(conversation);

        return message;
    }

    /**
     * Récupérer une conversation complète
     */
    @Transactional
    public List<Message> getConversation(String myEmail, String otherEmail) {

        LocalPartner partner = localPartnerRepo.findByEmail(otherEmail)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        Conversation conversation = conversationRepo
                .findBySenderEmailAndPartner(myEmail, partner)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Marquer comme lu selon qui consulte
        if (conversation.getSenderEmail().equals(myEmail)) {
            messageRepo.markMessagesAsRead(myEmail, conversation);  // CORRIGÉ: markMessagesAsRead au lieu de markAsRead
            conversation.setSenderViewed(true);
        } else {
            messageRepo.markMessagesAsRead(myEmail, conversation);  // CORRIGÉ: markMessagesAsRead au lieu de markAsRead
            conversation.setPartnerViewed(true);
        }

        conversationRepo.save(conversation);
        return messageRepo.findByConversationOrderBySentDateAsc(conversation);
    }

    /**
     * Récupérer toutes les conversations d'un expéditeur (investisseur ou partenaire éco)
     */
    public List<Conversation> getSenderConversations(String email) {
        return conversationRepo.findBySenderEmailOrderByLastMessageDateDesc(email);
    }

    /**
     * Récupérer toutes les conversations d'un partenaire local
     */
    public List<Conversation> getPartnerConversations(String email) {
        LocalPartner partner = localPartnerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Local partner not found"));
        return conversationRepo.findByPartnerOrderByLastMessageDateDesc(partner);
    }

    /**
     * Compter les messages non lus
     */
    public long countUnreadMessages(String email) {
        return messageRepo.countUnreadByRecipient(email);
    }

    /**
     * Récupérer la liste des messages non lus
     */
    public List<Message> getUnreadMessages(String email) {
        return messageRepo.findByRecipientEmailAndReadFalse(email);
    }

    /**
     * Vérifier si une conversation existe
     */
    public boolean conversationExists(String senderEmail, String partnerEmail) {
        LocalPartner partner = localPartnerRepo.findByEmail(partnerEmail).orElse(null);
        if (partner == null) return false;
        return conversationRepo.findBySenderEmailAndPartner(senderEmail, partner).isPresent();
    }
}