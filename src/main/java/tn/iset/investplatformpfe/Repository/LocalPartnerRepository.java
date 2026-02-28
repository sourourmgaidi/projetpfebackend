package tn.iset.investplatformpfe.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iset.investplatformpfe.Entity.LocalPartner;

import java.util.List;
import java.util.Optional;

public interface LocalPartnerRepository extends JpaRepository<LocalPartner, Long> {

    Optional<LocalPartner> findByEmail(String email);

    boolean existsByEmail(String email);

    // Méthode pour la recherche de partenaires - renommée en anglais
    @Query("SELECT p FROM LocalPartner p WHERE " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.activityDomain) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<LocalPartner> searchPartners(@Param("search") String search);  // Renommé de rechercherPartenaires à searchPartners

    // Optionnel: rechercher par domaine d'activité
    List<LocalPartner> findByActivityDomain(String activityDomain);

    // Optionnel: rechercher par statut
    List<LocalPartner> findByStatus(String status);

    // Optionnel: rechercher les partenaires actifs
    List<LocalPartner> findByActiveTrue();
    List<LocalPartner> findByFirstNameContainingOrLastNameContainingOrEmailContaining(String firstName, String lastName, String email);
}