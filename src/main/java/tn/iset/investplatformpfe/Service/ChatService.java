package tn.iset.investplatformpfe.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.iset.investplatformpfe.Entity.*;
import tn.iset.investplatformpfe.Repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatAdminUsersRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    // ========================================
    // CRÉER UNE CONVERSATION
    // ========================================
    @Transactional
    public ChatAdminUsers createConversation(
            String adminId, String adminEmail, String adminName,
            String userId, String userEmail, String userName, Role userRole) {

        var existingChat = chatRepository.findChatBetweenAdminAndUser(adminId, userId);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }

        ChatAdminUsers chat = new ChatAdminUsers();
        chat.setChatId(UUID.randomUUID().toString());
        chat.setAdminId(adminId);
        chat.setAdminEmail(adminEmail);
        chat.setAdminName(adminName);
        chat.setUserId(userId);
        chat.setUserEmail(userEmail);
        chat.setUserName(userName);
        chat.setUserRole(userRole);
        chat.setCreatedAt(LocalDateTime.now());

        return chatRepository.save(chat);
    }

    // ========================================
    // ENVOYER UN MESSAGE TEXTE
    // ========================================
    @Transactional
    public ChatMessage sendTextMessage(
            String chatId,
            String senderId, String senderEmail, String senderName, Role senderRole,
            String receiverId, String receiverEmail, String receiverName, Role receiverRole,
            String content) {

        ChatAdminUsers chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));

        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setSenderEmail(senderEmail);
        message.setSenderName(senderName);
        message.setSenderRole(senderRole);
        message.setReceiverId(receiverId);
        message.setReceiverEmail(receiverEmail);
        message.setReceiverName(receiverName);
        message.setReceiverRole(receiverRole);
        message.setContent(content);
        message.setType(MessageType.TEXT);
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = messageRepository.save(message);

        chat.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        chat.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(chat);

        return savedMessage;
    }

    // ========================================
    // ENVOYER UN MESSAGE AVEC FICHIERS
    // ========================================
    @Transactional
    public ChatMessage sendMessageWithFiles(
            String chatId,
            String senderId, String senderEmail, String senderName, Role senderRole,
            String receiverId, String receiverEmail, String receiverName, Role receiverRole,
            String content, MultipartFile[] files) throws Exception {

        ChatAdminUsers chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));

        // Déterminer le type de message
        MessageType type = MessageType.TEXT;
        if (files != null && files.length > 0) {
            String firstFileType = files[0].getContentType();
            type = (firstFileType != null && firstFileType.startsWith("image/")) ?
                    MessageType.IMAGE : MessageType.DOCUMENT;
        }

        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setSenderEmail(senderEmail);
        message.setSenderName(senderName);
        message.setSenderRole(senderRole);
        message.setReceiverId(receiverId);
        message.setReceiverEmail(receiverEmail);
        message.setReceiverName(receiverName);
        message.setReceiverRole(receiverRole);
        message.setContent(content);
        message.setType(type);
        message.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = messageRepository.save(message);

        // Gérer les fichiers
        if (files != null && files.length > 0) {
            List<ChatAttachment> attachments = new ArrayList<>();
            for (MultipartFile file : files) {
                ChatAttachment attachment = fileStorageService.storeFile(file, savedMessage.getMessageId());
                attachment.setMessage(savedMessage);
                attachments.add(attachmentRepository.save(attachment));
            }
            savedMessage.setAttachments(attachments);
        }

        // Mettre à jour la conversation
        String lastMsg = content != null && !content.isEmpty() ?
                (content.length() > 50 ? content.substring(0, 50) + "..." : content) :
                (files.length + " pièce(s) jointe(s)");

        chat.setLastMessage(lastMsg);
        chat.setLastMessageTime(LocalDateTime.now());
        chatRepository.save(chat);

        return savedMessage;
    }

    // ========================================
    // RÉCUPÉRER LES CONVERSATIONS D'UN UTILISATEUR
    // ========================================
    public List<ChatAdminUsers> getUserConversations(String userId, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return chatRepository.findAdminChats(userId);
        } else {
            return chatRepository.findUserChats(userId);
        }
    }

    // ========================================
    // RÉCUPÉRER LES MESSAGES D'UNE CONVERSATION
    // ========================================
    @Transactional
    public List<ChatMessage> getConversationMessages(String chatId, String currentUserId) {
        // Marquer les messages comme lus
        messageRepository.markMessagesAsRead(chatId, currentUserId);
        return messageRepository.findByChatChatIdOrderByTimestampAsc(chatId);
    }

    // ========================================
    // ✅ NOUVELLE MÉTHODE: MARQUER LES MESSAGES COMME LUS
    // ========================================
    @Transactional
    public void markMessagesAsRead(String chatId, String userId) {
        messageRepository.markMessagesAsRead(chatId, userId);
    }

    // ========================================
    // RÉCUPÉRER LES FICHIERS D'UNE CONVERSATION
    // ========================================
    public List<ChatAttachment> getConversationFiles(String chatId) {
        return attachmentRepository.findByChatId(chatId);
    }

    // ========================================
    // RÉCUPÉRER LES IMAGES D'UNE CONVERSATION
    // ========================================
    public List<ChatAttachment> getConversationImages(String chatId) {
        return attachmentRepository.findImagesByChatId(chatId);
    }

    // ========================================
    // COMPTER LES MESSAGES NON LUS
    // ========================================
    public long countUnreadMessages(String chatId, String userId) {
        return messageRepository.countUnreadMessages(chatId, userId);
    }

    // ========================================
    // SUPPRIMER UN MESSAGE
    // ========================================
    @Transactional
    public void deleteMessage(String messageId, String userId) {
        ChatMessage message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        // Vérifier que l'utilisateur est le propriétaire du message
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce message");
        }

        messageRepository.delete(message);
    }

    // ========================================
    // RECHERCHER DES MESSAGES
    // ========================================
    public List<ChatMessage> searchMessages(String userId, String keyword, String chatId) {
        if (chatId != null && !chatId.isEmpty()) {
            return messageRepository.searchInConversation(chatId, keyword);
        } else {
            return messageRepository.searchAllUserMessages(userId, keyword);
        }
    }

    // ========================================
    // COMPTER LES MESSAGES
    // ========================================
    public long countMessages(String chatId) {
        return messageRepository.countByChatId(chatId);
    }

    // ========================================
    // COMPTER LES FICHIERS
    // ========================================
    public long countFiles(String chatId) {
        return attachmentRepository.countByChatId(chatId);
    }

    // ========================================
    // COMPTER LES IMAGES
    // ========================================
    public long countImages(String chatId) {
        return attachmentRepository.countImagesByChatId(chatId);
    }

    // ========================================
    // DERNIÈRE ACTIVITÉ
    // ========================================
    public LocalDateTime getLastActivity(String chatId) {
        return messageRepository.getLastMessageTime(chatId);
    }
}