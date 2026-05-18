package com.agora.agoracampus.profile.background.model;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "background")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Background {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "background_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "individual_profile_id", nullable = false)
    private IndividualProfile individualProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BackgroundType type;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "currently_ongoing")
    private Boolean currentlyOngoing;
}
