package tn.iset.investplatformpfe.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderRole;           // ancien: expediteurRole

    @Column(nullable = false)
    private String senderEmail;          // ancien: expediteurEmail

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)  // changé de "partenaire_id" à "partner_id"
    private LocalPartner partner;

    @Column(name = "last_message")       // ancien: dernierMessage
    private String lastMessage;

    @Column(name = "last_message_date")  // ancien: dateDernierMessage
    private LocalDateTime lastMessageDate;

    @Column(name = "sender_viewed")      // ancien: expediteurVu
    private boolean senderViewed = true;

    @Column(name = "partner_viewed")     // ancien: partenaireVu
    private boolean partnerViewed = true;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Message> messages = new ArrayList<>();

    public Conversation() {}

    // Constructeur avec paramètres
    public Conversation(String senderRole, String senderEmail, LocalPartner partner) {
        this.senderRole = senderRole;
        this.senderEmail = senderEmail;
        this.partner = partner;
        this.lastMessageDate = LocalDateTime.now();
        this.senderViewed = true;
        this.partnerViewed = false;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public LocalPartner getPartner() {
        return partner;
    }

    public void setPartner(LocalPartner partner) {
        this.partner = partner;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(LocalDateTime lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public boolean isSenderViewed() {
        return senderViewed;
    }

    public void setSenderViewed(boolean senderViewed) {
        this.senderViewed = senderViewed;
    }

    public boolean isPartnerViewed() {
        return partnerViewed;
    }

    public void setPartnerViewed(boolean partnerViewed) {
        this.partnerViewed = partnerViewed;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}