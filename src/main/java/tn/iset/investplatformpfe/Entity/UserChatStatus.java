package tn.iset.investplatformpfe.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_status")
@Data
public class UserChatStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // ID Keycloak ou email

    private String userEmail;
    private String userName;

    @Enumerated(EnumType.STRING)
    private Role userRole;

    private boolean isOnline = false;
    private LocalDateTime lastSeen;

    private String currentSessionId;

    @PreUpdate
    protected void onUpdate() {
        lastSeen = LocalDateTime.now();
    }
}
