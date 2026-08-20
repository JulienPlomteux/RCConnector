package com.plomteux.rcconnector.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * Idempotently creates the indexes backing GET /cruise-history/{code}.
 *
 * Done with raw DDL after the schema exists instead of @Table(indexes=...):
 * Hibernate 6.1 fails schema export when an index targets a @JoinColumn FK
 * column ("column was not found"), which would break startup on ddl-auto=update.
 * Statement is safe on PostgreSQL and H2.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HistoryIndexInitializer {

    private static final List<String> DDL = List.of(
            "CREATE INDEX IF NOT EXISTS idx_cruise_code ON cruise_details_entity (code)",
            "CREATE INDEX IF NOT EXISTS idx_sailings_cruise_fk ON sailings_entity (cruise_details_entity_id)");

    private final DataSource dataSource;

    @PostConstruct
    void ensureIndexes() {
        for (String ddl : DDL) {
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
                s.execute(ddl);
            } catch (Exception e) {
                // non-fatal: the endpoint still works, just slower
                log.warn("Index initialization skipped for [{}]: {}", ddl, e.toString());
            }
        }
    }
}
