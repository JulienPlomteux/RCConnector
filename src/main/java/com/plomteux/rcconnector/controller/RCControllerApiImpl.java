package com.plomteux.rcconnector.controller;

import com.plomteux.rcconnector.model.CruiseOverView;
import com.plomteux.rcconnector.repository.SailingsRepository;
import com.plomteux.rcconnector.service.RCService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
@Slf4j
public class RCControllerApiImpl implements RCControllerApi {
    private final RCService rCService;
    private final SailingsRepository sailingsRepository;
    @Override
    public ResponseEntity<Void> getCruiseDetails() {
        log.debug("Received getCruiseDetails request");
        rCService.getAllCruisesDetails();
        return ResponseEntity.noContent().build();
    }
    @Override
    public ResponseEntity<List<CruiseOverView>> getSailingsPriceDrops(
            @RequestParam("fromDate") LocalDate fromDate,
            @RequestParam("toDate") LocalDate toDate,
            @RequestParam("percentage") BigDecimal percentage,
            @RequestParam("roomType") String roomType) {
        LocalDate fromDateParsed = fromDate != null ? fromDate : LocalDate.now().minusDays(1);
        LocalDate toDateParsed = toDate != null ? toDate : LocalDate.now();
        roomType = roomType != null ? roomType : "inside";
        percentage = percentage != null ? percentage : BigDecimal.ZERO;
        if (fromDateParsed.isAfter(toDateParsed)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Percentage cannot be negative");
        }
        if (percentage.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Percentage cannot be 100% or above");
        }

        log.debug("Received getSailingsPriceDrops request");

        List<CruiseOverView> results = sailingsRepository.getSailingsPriceDrops(
                fromDateParsed,
                toDateParsed,
                percentage,
                roomType
        );
        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity<List<CruiseOverView>> findCruise(
            @RequestParam("departureDate") LocalDate departureDate,
            @RequestParam("returnDate") LocalDate returnDate,
            @RequestParam("priceUpTo") BigDecimal priceUpTo,
            @RequestParam("priceFrom") BigDecimal priceFrom,
            @RequestParam("daysAtSeaMin") BigDecimal daysAtSeaMin,
            @RequestParam("daysAtSeaMax") BigDecimal daysAtSeaMax,
            @RequestParam("departurePort") String departurePort,
            @RequestParam("destinationCode") String destinationCode) {
        log.debug("Received findCruise request");
        if (departureDate.isAfter(returnDate)) {
            throw new IllegalArgumentException("Departure date cannot be after return date");
        }
        if (priceUpTo != null && priceFrom != null && priceUpTo.compareTo(priceFrom) < 0) {
            throw new IllegalArgumentException("Price up to cannot be less than price from");
        }
        if (daysAtSeaMax != null && daysAtSeaMin != null && daysAtSeaMax.compareTo(daysAtSeaMin) < 0) {
            throw new IllegalArgumentException("Days at sea max cannot be less than days at sea min");
        }
        List<CruiseOverView> results = sailingsRepository.findCruise(departureDate, returnDate, priceUpTo, priceFrom, daysAtSeaMin, daysAtSeaMax, departurePort, destinationCode);
        return ResponseEntity.ok(results);
    }
}
