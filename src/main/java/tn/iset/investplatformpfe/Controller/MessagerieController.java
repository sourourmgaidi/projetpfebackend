package tn.iset.investplatformpfe.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tn.iset.investplatformpfe.Entity.Message;
import tn.iset.investplatformpfe.Entity.Conversation;
import tn.iset.investplatformpfe.Service.MessagerieService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/messagerie")
public class MessagerieController {

    private final MessagerieService messagerieService;

    public MessagerieController(MessagerieService messagerieService) {
        this.messagerieService = messagerieService;
    }

    // ==================== POUR TOUS LES EXPÉDITEURS (INVESTOR, PARTNER) ====================

    /**
     * Rechercher des partenaires locaux
     */
    @GetMapping("/search-local-partners")  // Renommé
    public ResponseEntity<?> searchLocalPartners(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String q) {

        if (!hasAnyRole(jwt, "INVESTOR", "PARTNER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        if (q.length() < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "Minimum 2 characters required"));
        }

        return ResponseEntity.ok(messagerieService.searchLocalPartners(q));  // Renommé
    }

    /**
     * Rechercher dans ses conversations
     */
    @GetMapping("/search-conversations")  // Renommé
    public ResponseEntity<?> searchConversations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String q) {

        if (!hasAnyRole(jwt, "INVESTOR", "PARTNER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(messagerieService.searchSenderConversations(email, q));  // Renommé
    }

    /**
     * Envoyer un message à un partenaire local (crée la conversation automatiquement)
     */
    @PostMapping("/send")  // Renommé
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> request) {

        String role = getRole(jwt);
        if (!hasAnyRole(jwt, "INVESTOR", "PARTNER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        String senderEmail = jwt.getClaimAsString("email");
        String partnerEmail = request.get("partnerEmail");  // Renommé
        String content = request.get("content");  // Renommé

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        try {
            Message message = messagerieService.sendMessage(senderEmail, partnerEmail, content, role);  // Renommé
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Voir ses conversations (en tant qu'expéditeur)
     */
    @GetMapping("/my-conversations")  // Renommé
    public ResponseEntity<?> getMyConversations(@AuthenticationPrincipal Jwt jwt) {
        if (!hasAnyRole(jwt, "INVESTOR", "PARTNER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(messagerieService.getSenderConversations(email));  // Renommé
    }

    // ==================== POUR LES PARTENAIRES LOCAUX ====================

    /**
     * Partenaire local : voir ses conversations
     */
    @GetMapping("/local-partner/my-conversations")  // Renommé
    public ResponseEntity<?> getPartnerConversations(@AuthenticationPrincipal Jwt jwt) {
        if (!hasRole(jwt, "LOCAL_PARTNER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied for local partners only"));
        }

        String email = jwt.getClaimAsString("email");
        return ResponseEntity.ok(messagerieService.getPartnerConversations(email));  // Renommé
    }

    /**
     * Partenaire local : répondre à un message
     */
    @PostMapping("/local-partner/reply")  // Renommé
    public ResponseEntity<?> replyMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> request) {

        if (!hasRole(jwt, "LOCAL_PARTNER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied for local partners only"));
        }

        String partnerEmail = jwt.getClaimAsString("email");
        String senderEmail = request.get("senderEmail");  // Renommé
        String content = request.get("content");  // Renommé

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
        }

        try {
            Message message = messagerieService.replyMessage(partnerEmail, senderEmail, content);  // Renommé
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== POUR TOUS ====================

    /**
     * Voir une conversation spécifique
     */
    @GetMapping("/conversation/{otherEmail}")  // Renommé paramètre
    public ResponseEntity<?> getConversation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String otherEmail) {

        String myEmail = jwt.getClaimAsString("email");
        List<Message> messages = messagerieService.getConversation(myEmail, otherEmail);
        return ResponseEntity.ok(messages);
    }

    /**
     * Voir les messages non lus
     */
    @GetMapping("/unread")  // Renommé
    public ResponseEntity<?> getUnreadMessages(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String email = jwt.getClaimAsString("email");

        long count = messagerieService.countUnreadMessages(email);  // Renommé
        List<Message> unreadMessages = messagerieService.getUnreadMessages(email);  // Renommé

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);  // Renommé
        response.put("messages", unreadMessages);

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si une conversation existe
     */
    @GetMapping("/exists/{partnerEmail}")  // Nouvel endpoint
    public ResponseEntity<?> conversationExists(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String partnerEmail) {

        String senderEmail = jwt.getClaimAsString("email");
        boolean exists = messagerieService.conversationExists(senderEmail, partnerEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // ==================== UTILITAIRES ====================

    private boolean hasRole(Jwt jwt, String role) {
        if (jwt == null) return false;
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles != null && roles.contains(role);
        }
        return false;
    }

    private boolean hasAnyRole(Jwt jwt, String... roles) {
        if (jwt == null) return false;
        for (String role : roles) {
            if (hasRole(jwt, role)) return true;
        }
        return false;
    }

    private String getRole(Jwt jwt) {
        if (jwt == null) return null;
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null && !roles.isEmpty()) {
                return roles.get(0);
            }
        }
        return null;
    }
}