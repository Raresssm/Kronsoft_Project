package com.agora.agoracampus.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="background")
public class Background {

@Id
@Column
@GeneratedValue(strategy= GenerationType.IDENTITY)
private Long backgroundId;


@ManyToOne
    @JoinColumn(name="individualProfileId",nullable = false)
    private IndividualProfile individualProfile;



@Enumerated(EnumType.STRING) // salveaza ca string in DB
private BackgroundType type;

    @NotBlank
    private String title;
    private String description;

    @NotNull
    @Column
    private LocalDate startDate;
    @Column
    private LocalDate endDate;
    @Column
    private Boolean currentlyOngoing;



}
