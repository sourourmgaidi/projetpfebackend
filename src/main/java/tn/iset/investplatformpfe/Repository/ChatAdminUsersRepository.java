package tn.iset.investplatformpfe.Repository;

import tn.iset.investplatformpfe.Entity.ChatAdminUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatAdminUsersRepository extends JpaRepository<ChatAdminUsers, Long> {

    Optional<ChatAdminUsers> findByChatId(String chatId);

    @Query("SELECT c FROM ChatAdminUsers c WHERE c.userId = :userId ORDER BY c.lastMessageTime DESC NULLS LAST")
    List<ChatAdminUsers> findUserChats(@Param("userId") String userId);

    @Query("SELECT c FROM ChatAdminUsers c WHERE c.adminId = :adminId ORDER BY c.lastMessageTime DESC NULLS LAST")
    List<ChatAdminUsers> findAdminChats(@Param("adminId") String adminId);

    @Query("SELECT c FROM ChatAdminUsers c WHERE c.adminId = :adminId AND c.userId = :userId")
    Optional<ChatAdminUsers> findChatBetweenAdminAndUser(
            @Param("adminId") String adminId,
            @Param("userId") String userId
    );

    @Query("SELECT c FROM ChatAdminUsers c WHERE c.userId = :userId AND c.lastMessageTime IS NOT NULL")
    List<ChatAdminUsers> findActiveUserChats(@Param("userId") String userId);

    @Query("SELECT COUNT(c) FROM ChatAdminUsers c WHERE c.adminId = :adminId")
    long countAdminChats(@Param("adminId") String adminId);

    @Query("SELECT COUNT(c) FROM ChatAdminUsers c WHERE c.userId = :userId")
    long countUserChats(@Param("userId") String userId);
}