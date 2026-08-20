package com.plomteux.rcconnector.repository;

import com.plomteux.rcconnector.entity.SailingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SailingsRepository extends JpaRepository<SailingsEntity, Long>, SailingsRepositoryCustom {

    /**
     * One-shot full price history of an itinerary code: the same code is stored
     * across hundreds of duplicate cruise entities (one snapshot each), so this
     * single join query replaces ~200 per-entity lookups.
     */
    @Query("""
            select s from SailingsEntity s
            join s.cruiseDetailsEntity c
            where c.code = :code
            order by s.departureDate, s.publishedDate, s.id
            """)
    List<SailingsEntity> findHistoryByCruiseCode(@Param("code") String code);
}