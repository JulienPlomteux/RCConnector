package com.plomteux.rcconnector.repository;

import com.plomteux.rcconnector.entity.RoomType;
import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.mapper.CruiseOverViewMapper;
import com.plomteux.rcconnector.model.CruiseOverView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class SailingsRepositoryCustomImpl implements SailingsRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final CruiseOverViewMapper cruiseOverViewMapper;

    private static final String CRUISE_DETAILS_ENTITY = "cruiseDetailsEntity";
    private static final String SAIL_ID = "sailId";
    private static final String PUBLISHED_DATE = "publishedDate";
    private static final String INSIDE = "inside";
    private static final String BUNDLE_TYPE = "bundleType";
    private static final String PACKAGE_ID = "packageId";
    private static final String DEPARTURE_DATE = "departureDate";
    private static final String RETURN_DATE = "returnDate";
    private static final String EMBARKATION_PORT_CODE = "embarkationPortCode";
    private static final String DURATION = "duration";

    @Override
    public List<CruiseOverView> getSailingsPriceDrops(LocalDate from, LocalDate to, BigDecimal percentage, String roomType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<SailingsEntity> s = cq.from(SailingsEntity.class);
        Root<SailingsEntity> s2 = cq.from(SailingsEntity.class);

        String roomTypeField;
        try {
            roomTypeField = RoomType.valueOf(roomType.toUpperCase()).getFieldName();
        } catch (IllegalArgumentException e) {
            roomTypeField = RoomType.INSIDE.getFieldName();
        }

        Expression<BigDecimal> priceDifference = cb.diff(
                s.get(roomTypeField),
                s2.get(roomTypeField)
        );


        Predicate sameSailId = cb.equal(s.get(SAIL_ID), s2.get(SAIL_ID));

        Expression<LocalDate> fromExpression = cb.literal(from);
        Expression<LocalDate> toExpression = cb.literal(to);
        Predicate publishedDateConditionS = cb.equal(s.get(PUBLISHED_DATE).as(LocalDate.class), fromExpression);
        Predicate publishedDateConditionS2 = cb.equal(s2.get(PUBLISHED_DATE).as(LocalDate.class), toExpression);

        Expression<BigDecimal> percentageExpression = cb.literal(percentage);
        Expression<BigDecimal> reducedPrice = cb.prod(s.get(roomTypeField), cb.diff(cb.literal(BigDecimal.ONE), percentageExpression));
        Predicate priceDropCondition;
        if (percentage.compareTo(BigDecimal.ZERO) == 0) {
            priceDropCondition = cb.lt(s2.get(roomTypeField), s.get(roomTypeField));
        } else {
            priceDropCondition = cb.le(s2.get(roomTypeField), reducedPrice);
        }

        cq.multiselect(s2, priceDifference)
                .where(publishedDateConditionS, publishedDateConditionS2, sameSailId, priceDropCondition)
                .orderBy(cb.desc(priceDifference));

        return entityManager.createQuery(cq).getResultList().stream()
                .map(result -> {
                    SailingsEntity sailings = result.get(0, SailingsEntity.class);
                    BigDecimal priceDrop = result.get(1, BigDecimal.class);
                    return cruiseOverViewMapper.toCruiseOverView(sailings, priceDrop);
                })
                .toList();
    }

    @Override
    public List<CruiseOverView> findCruise(LocalDate departureDate, LocalDate returnDate, BigDecimal priceUpTo, BigDecimal priceFrom, BigDecimal daysAtSeaMin, BigDecimal daysAtSeaMax, String departurePort, String destinationCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SailingsEntity> cq = cb.createQuery(SailingsEntity.class);

        Root<SailingsEntity> sailings = cq.from(SailingsEntity.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.greaterThanOrEqualTo(sailings.get(DEPARTURE_DATE), departureDate));
        predicates.add(cb.lessThanOrEqualTo(sailings.get(RETURN_DATE), returnDate));
        predicates.add(cb.equal(sailings.get(PUBLISHED_DATE), LocalDate.now()));

        if (destinationCode != null) {
            predicates.add(cb.equal(sailings.get(CRUISE_DETAILS_ENTITY).get("destination"), destinationCode));
        }
        if (priceFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(sailings.get(INSIDE), priceFrom));
        }
        if (priceUpTo != null) {
            predicates.add(cb.lessThanOrEqualTo(sailings.get(INSIDE), priceUpTo));
        }
        if (departurePort != null) {
            predicates.add(cb.equal(sailings.get(CRUISE_DETAILS_ENTITY).get(EMBARKATION_PORT_CODE), departurePort));
        }
        if (daysAtSeaMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(sailings.get(CRUISE_DETAILS_ENTITY).get(DURATION), daysAtSeaMin));
        }
        if (daysAtSeaMax != null) {
            predicates.add(cb.lessThanOrEqualTo(sailings.get(CRUISE_DETAILS_ENTITY).get(DURATION), daysAtSeaMax));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(sailings.get(INSIDE)));

        return entityManager.createQuery(cq).getResultList().stream()
                .map(result -> cruiseOverViewMapper.toCruiseOverView(result, BigDecimal.ZERO))
                .toList();
    }
}