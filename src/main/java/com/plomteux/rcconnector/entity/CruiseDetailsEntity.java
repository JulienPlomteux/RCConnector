package com.plomteux.rcconnector.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Setter
@Getter
public class CruiseDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private BigDecimal duration;
    private String embarkationPortCode;
    private String destination;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cruiseDetailsEntity")
    private List<SailingsEntity> sailingsEntities;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cruiseDetailsEntity")
    private List<DestinationCodeEntity> destinationsEntities;
}
