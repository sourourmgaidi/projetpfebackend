package tn.iset.investplatformpfe.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5000)
    private String content;  // ancien: contenu

    @Column(nullable = false)
    private String senderEmail;  // ancien: expediteurEmail

    @Column(nullable = false)
    private String recipientEmail;  // ancien: destinataireEmail

    @Column(nullable = false)
    private LocalDateTime sentDate;  // ancien: dateEnvoi

    private boolean read = false;  // ancien: lu

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    public Message() {}

    public Message(String content, String senderEmail, String recipientEmail, Conversation conversation) {
        this.content = content;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.conversation = conversation;
        this.sentDate = LocalDateTime.now();
        this.read = false;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}