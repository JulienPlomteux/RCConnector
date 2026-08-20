package com.plomteux.rcconnector;

import com.plomteux.rcconnector.entity.CruiseDetailsEntity;
import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.repository.CruiseDetailsRepository;
import com.plomteux.rcconnector.repository.SailingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One-shot history endpoint: one code spread across duplicate cruise entities
 * (the RC/CBC duplicate pattern) must come back in a single request.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CruiseHistoryControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    CruiseDetailsRepository cruiseDetailsRepository;

    @Autowired
    SailingsRepository sailingsRepository;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.update("delete from sailings_entity");
        jdbc.update("delete from cruise_details_entity");
    }

    private long seedSailing(String code, LocalDate departure, LocalDate published, BigDecimal inside) {
        CruiseDetailsEntity cruise = new CruiseDetailsEntity();
        cruise.setCode(code);
        cruiseDetailsRepository.saveAndFlush(cruise);

        SailingsEntity s = new SailingsEntity();
        s.setCruiseDetailsEntity(cruise);
        s.setDepartureDate(departure);
        s.setReturnDate(departure.plusDays(7));
        s.setInside(inside);
        s.setOceanView(inside != null ? inside.add(BigDecimal.TEN) : null);
        s.setBalcony(inside != null ? inside.add(BigDecimal.valueOf(100)) : null);
        s.setBookingLink("https://example.com/booking?groupId=" + code);
        sailingsRepository.saveAndFlush(s);

        // @PrePersist overwrites publishedDate with today — pin it for the test
        jdbc.update("update sailings_entity set published_date = ? where id = ?",
                published, s.getId());
        return cruise.getId();
    }

    @Test
    void oneShotReturnsEveryDuplicateSnapshotOfOneCodeOnly() {
        LocalDate dep = LocalDate.parse("2026-11-05");
        seedSailing("CODEA", dep, LocalDate.parse("2026-08-01"), new BigDecimal("100"));
        seedSailing("CODEA", dep, LocalDate.parse("2026-08-02"), new BigDecimal("90"));
        seedSailing("CODEA", dep, LocalDate.parse("2026-08-03"), new BigDecimal("80"));
        seedSailing("CODEB", dep, LocalDate.parse("2026-08-01"), new BigDecimal("500"));

        ResponseEntity<JsonPoint[]> res = rest.getForEntity(
                "http://localhost:" + port + "/cruise-history/CODEA", JsonPoint[].class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).hasSize(3);
        // oldest publishedDate first
        assertThat(res.getBody()[0].publishedDate).isEqualTo("2026-08-01");
        assertThat(res.getBody()[2].publishedDate).isEqualTo("2026-08-03");
        assertThat(res.getBody()[0].inside).isEqualByComparingTo("100");
        assertThat(res.getBody()[2].inside).isEqualByComparingTo("80");
        assertThat(res.getBody()[0].cruiseEntityId).isNotNull();
        assertThat(res.getBody()[0].bookingLink).contains("groupId=CODEA");

        ResponseEntity<JsonPoint[]> other = rest.getForEntity(
                "http://localhost:" + port + "/cruise-history/CODEB", JsonPoint[].class);
        assertThat(other.getBody()).hasSize(1);
        assertThat(other.getBody()[0].inside).isEqualByComparingTo("500");

        ResponseEntity<JsonPoint[]> missing = rest.getForEntity(
                "http://localhost:" + port + "/cruise-history/NOSUCH", JsonPoint[].class);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(missing.getBody()).isEmpty();
    }

    /** Loose shape for assertions (Jackson maps by field name). */
    record JsonPoint(
            Long cruiseEntityId,
            String departureDate,
            String returnDate,
            String publishedDate,
            BigDecimal inside,
            BigDecimal oceanView,
            BigDecimal balcony,
            String bookingLink) {
    }
}
