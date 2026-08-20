-- Indexes for the one-shot GET /cruise-history/{code} endpoint.
-- If spring.jpa.hibernate.ddl-auto=update is active they are also created
-- automatically on the next restart (see @Index on the entities); run this
-- manually when ddl-auto is off/validate. Apply to BOTH the RC and the
-- Celebrity (CBC) databases — same codebase.
--
-- Adjust table/column names if your schema uses different physical names
-- (Spring Boot defaults shown here).

CREATE INDEX IF NOT EXISTS idx_cruise_code ON cruise_details_entity (code);
CREATE INDEX IF NOT EXISTS idx_sailings_cruise_fk ON sailings_entity (cruise_details_entity_id);
