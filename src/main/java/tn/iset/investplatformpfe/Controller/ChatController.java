package tn.iset.investplatformpfe.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.iset.investplatformpfe.Entity.ChatAdminUsers;
import tn.iset.investplatformpfe.Entity.ChatAttachment;
import tn.iset.investplatformpfe.Entity.ChatMessage;
import tn.iset.investplatformpfe.Entity.Role;
import tn.iset.investplatformpfe.Service.ChatService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ========================================
    // CRÉER UNE CONVERSATION (Uniquement pour les admins)
    // ========================================
    @PostMapping("/conversation")
    public ResponseEntity<?> createConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> request) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        try {
            // Vérifier que l'utilisateur connecté est un ADMIN
            String userRole = jwt.getClaimAsString("role");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(403).body(Map.of("error", "Seuls les administrateurs peuvent créer des conversations"));
            }

            // L'admin est celui qui est connecté
            String adminId = jwt.getClaimAsString("sub");
            String adminEmail = jwt.getClaimAsString("email");
            String adminName = jwt.getClaimAsString("given_name") + " " + jwt.getClaimAsString("family_name");

            String userId = request.get("userId");
            String userEmail = request.get("userEmail");
            String userName = request.get("userName");
            String userRole2 = request.get("userRole");

            if (userId == null || userEmail == null || userName == null || userRole2 == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tous les champs sont requis"));
            }

            ChatAdminUsers chat = chatService.createConversation(
                    adminId, adminEmail, adminName,
                    userId, userEmail, userName, Role.valueOf(userRole2)
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conversation créée avec succès",
                    "chat", chat
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rôle invalide: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // ENVOYER UN MESSAGE TEXTE
    // ========================================
    @PostMapping("/message/text")
    public ResponseEntity<?> sendTextMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> request) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        try {
            String senderId = jwt.getClaimAsString("sub");
            String senderEmail = jwt.getClaimAsString("email");
            String senderName = jwt.getClaimAsString("given_name") + " " + jwt.getClaimAsString("family_name");
            String senderRole = jwt.getClaimAsString("role");

            String chatId = request.get("chatId");
            String receiverId = request.get("receiverId");
            String receiverEmail = request.get("receiverEmail");
            String receiverName = request.get("receiverName");
            String receiverRole = request.get("receiverRole");
            String content = request.get("content");

            if (chatId == null || receiverId == null || receiverEmail == null ||
                    receiverName == null || receiverRole == null || content == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tous les champs sont requis"));
            }

            ChatMessage message = chatService.sendTextMessage(
                    chatId,
                    senderId, senderEmail, senderName, Role.valueOf(senderRole),
                    receiverId, receiverEmail, receiverName, Role.valueOf(receiverRole),
                    content
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message envoyé avec succès",
                    "data", message
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rôle invalide: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // ENVOYER UN MESSAGE AVEC FICHIERS
    // ========================================
    @PostMapping(value = "/message/files", consumes = {"multipart/form-data"})
    public ResponseEntity<?> sendMessageWithFiles(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("chatId") String chatId,
            @RequestParam("receiverId") String receiverId,
            @RequestParam("receiverEmail") String receiverEmail,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverRole") String receiverRole,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        try {
            String senderId = jwt.getClaimAsString("sub");
            String senderEmail = jwt.getClaimAsString("email");
            String senderName = jwt.getClaimAsString("given_name") + " " + jwt.getClaimAsString("family_name");
            String senderRole = jwt.getClaimAsString("role");

            if (chatId == null || receiverId == null || receiverEmail == null || receiverName == null || receiverRole == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tous les champs sont requis"));
            }

            ChatMessage message = chatService.sendMessageWithFiles(
                    chatId,
                    senderId, senderEmail, senderName, Role.valueOf(senderRole),
                    receiverId, receiverEmail, receiverName, Role.valueOf(receiverRole),
                    content, files
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Message envoyé avec succès");
            response.put("data", message);

            if (files != null && files.length > 0) {
                response.put("fileCount", files.length);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rôle invalide: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // RÉCUPÉRER MES CONVERSATIONS
    // ========================================
    @GetMapping("/conversations")
    public ResponseEntity<?> getMyConversations(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        try {
            String userId = jwt.getClaimAsString("sub");
            String userRole = jwt.getClaimAsString("role");

            List<ChatAdminUsers> conversations = chatService.getUserConversations(userId, userRole);

            // Ajouter le nombre de messages non lus pour chaque conversation
            List<Map<String, Object>> result = conversations.stream().map(conv -> {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", conv);
                map.put("unreadCount", chatService.countUnreadMessages(conv.getChatId(), userId));
                return map;
            }).toList();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "conversations", result
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // RÉCUPÉRER LES MESSAGES D'UNE CONVERSATION
    // ========================================
    @GetMapping("/messages/{chatId}")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String chatId) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        try {
            String userId = jwt.getClaimAsString("sub");
            List<ChatMessage> messages = chatService.getConversationMessages(chatId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messages", messages
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // RÉCUPÉRER LES FICHIERS D'UNE CONVERSATION
    // ========================================
    @GetMapping("/files/{chatId}")
    public ResponseEntity<?> getConversationFiles(@PathVariable String chatId) {
        try {
            List<ChatAttachment> files = chatService.getConversationFiles(chatId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "files", files,
                    "count", files.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // RÉCUPÉRER LES IMAGES D'UNE CONVERSATION
    // ========================================
    @GetMapping("/images/{chatId}")
    public ResponseEntity<?> getConversationImages(@PathVariable String chatId) {
        try {
            List<ChatAttachment> images = chatService.getConversationImages(chatId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "images", images,
                    "count", images.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // MARQUER LES MESSAGES COMME LUS
    // ========================================
    @PostMapping("/read/{chatId}")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String chatId) {

        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        try {
            String userId = jwt.getClaimAsString("sub");
            chatService.markMessagesAsRead(chatId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Messages marqués comme lus"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}