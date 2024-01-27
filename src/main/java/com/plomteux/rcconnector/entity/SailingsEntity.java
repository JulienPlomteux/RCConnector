package com.plomteux.rcconnector.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Setter
@Getter
public class SailingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sailId;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private LocalDate publishedDate;
    private BigDecimal inside;
    private BigDecimal oceanView;
    private BigDecimal balcony;
    private BigDecimal oldPrice;
    private String bookingLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cruiseDetailsEntity_id")
    private CruiseDetailsEntity cruiseDetailsEntity;

    @PrePersist
    public void prePersist() {
        this.publishedDate = LocalDate.now();
    }
}
