package com.plomteux.rcconnector.repository;

import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.model.CruiseOverView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SailingsRepositoryCustom {
    List<CruiseOverView> getSailingsPriceDrops(LocalDate from, LocalDate to, BigDecimal percentage, String roomType);
    List<CruiseOverView> findCruise(LocalDate departureDate, LocalDate returnDate, BigDecimal priceUpTo, BigDecimal priceFrom, BigDecimal daysAtSeaMin, BigDecimal daysAtSeaMax, String departurePort, String destinationCode);

    }