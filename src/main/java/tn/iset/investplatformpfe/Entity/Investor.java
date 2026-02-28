package tn.iset.investplatformpfe.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;


@Table(name = "investor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstName", nullable = false)  // NON NULL
    private String firstName;

    @Column(name = "lastName", nullable = false)   // NON NULL
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)  // NON NULL
    private String email;

    @Column(name = "password", nullable = false)  // NON NULL
    private String password;

    @Column(name = "phone", nullable = false)  // NON NULL
    private String phone;

    @Column(name = "profile_picture", nullable = false)  // NON NULL
    private String profilePicture;

    @Column(name = "registration_date", nullable = false)  // NON NULL
    private LocalDateTime registrationDate;

    @Column(name = "active", nullable = false)  // NON NULL
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)  // NON NULL
    private Role role = Role.INVESTOR;

    @Column(name = "company", nullable = false)  // NON NULL
    private String company;

    @Column(name = "origin_country", nullable = false)  // NON NULL
    private String originCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_sector", nullable = false)  // NON NULL
    private ActivityDomain activitySector;

    @Column(name = "website", nullable = false)  // NON NULL
    private String website;

    @Column(name = "linkedin_profile", nullable = false)  // NON NULL
    private String linkedinProfile;

    @Column(name = "nationality")
    private String nationality;

    @ManyToMany(mappedBy = "interestedInvestors")
    private List<InvestmentService> interestedInvestmentServices;

    // Getters et Setters (générés par Lombok @Data)
}