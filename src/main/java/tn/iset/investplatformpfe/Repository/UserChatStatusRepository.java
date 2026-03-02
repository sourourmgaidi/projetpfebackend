package tn.iset.investplatformpfe.Repository;
import tn.iset.investplatformpfe.Entity.UserChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserChatStatusRepository extends JpaRepository<UserChatStatus, Long> {

    Optional<UserChatStatus> findByUserId(String userId);

    List<UserChatStatus> findByIsOnlineTrue();

    List<UserChatStatus> findByUserRole(tn.iset.investplatformpfe.Entity.Role role);
}