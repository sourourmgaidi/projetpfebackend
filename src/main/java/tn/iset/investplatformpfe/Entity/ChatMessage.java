package tn.iset.investplatformpfe.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatAdminUsers chat;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role senderRole;

    @Column(nullable = false)
    private String receiverId;

    @Column(nullable = false)
    private String receiverEmail;

    @Column(nullable = false)
    private String receiverName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role receiverRole;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type;  // Maintenant accessible car MessageType est public

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private boolean isRead = false;
    private LocalDateTime readAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<ChatAttachment> attachments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}