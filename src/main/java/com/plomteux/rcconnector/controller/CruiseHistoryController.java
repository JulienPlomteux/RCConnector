package com.plomteux.rcconnector.controller;

import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.repository.SailingsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One-shot full price history of an itinerary code.
 *
 * The scraper re-inserts the same itinerary as a NEW cruiseDetailsEntity on
 * every daily scrape, so one code is spread across hundreds of duplicate
 * entities (each holding ~1 snapshot). The BFF used to fetch those ~200
 * entities one HTTP request at a time; this endpoint serves the whole
 * history with a single indexed join query.
 */
@AllArgsConstructor
@RestController
@Slf4j
public class CruiseHistoryController {

    private final SailingsRepository sailingsRepository;

    public record HistoryPoint(
            Long cruiseEntityId,
            LocalDate departureDate,
            LocalDate returnDate,
            LocalDate publishedDate,
            BigDecimal inside,
            BigDecimal oceanView,
            BigDecimal balcony,
            String bookingLink) {

        static HistoryPoint from(SailingsEntity s) {
            return new HistoryPoint(
                    s.getCruiseDetailsEntity() != null ? s.getCruiseDetailsEntity().getId() : null,
                    s.getDepartureDate(),
                    s.getReturnDate(),
                    s.getPublishedDate(),
                    s.getInside(),
                    s.getOceanView(),
                    s.getBalcony(),
                    s.getBookingLink());
        }
    }

    @GetMapping("/cruise-history/{code}")
    public ResponseEntity<List<HistoryPoint>> history(@PathVariable String code) {
        List<HistoryPoint> points = sailingsRepository.findHistoryByCruiseCode(code).stream()
                .map(HistoryPoint::from)
                .toList();
        return ResponseEntity.ok(points);
    }
}
