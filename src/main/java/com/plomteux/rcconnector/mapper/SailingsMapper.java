package com.plomteux.rcconnector.mapper;

import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.model.Sailings;
import com.plomteux.rcconnector.model.SailingsStateroomClassPricingInner;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface SailingsMapper {
    Logger log = LoggerFactory.getLogger(SailingsMapper.class);

    @Mapping(target = "sailId", source = "id")
    @Mapping(target = "departureDate", source = "startDate")
    @Mapping(target = "returnDate", source = "endDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingLink", ignore = true)
    SailingsEntity toSailingsEntity(Sailings sailings);

//    @Mapping(target = "pricing", ignore = true)
//    Sailings toSailings(SailingsEntity sailingsEntity);

    @BeforeMapping
    default void mapStateroomClassPricing(Sailings sailings, @MappingTarget SailingsEntity sailingsEntity) {

        for (SailingsStateroomClassPricingInner pricing : sailings.getStateroomClassPricing()) {
            String roomType = pricing.getStateroomClass().getId();
            try {
                switch (roomType) {
                    case "INTERIOR" -> sailingsEntity.setInside(pricing.getPrice().getValue());
                    case "OUTSIDE" -> sailingsEntity.setOceanView(pricing.getPrice().getValue());
                    case "BALCONY" -> sailingsEntity.setBalcony(pricing.getPrice().getValue());
                }
            } catch (NullPointerException e) {
                log.warn(String.format("Sailing: %s does not have a price for room: %s", sailings.getId(), roomType));

            }
        }
        sailingsEntity.setBookingLink("https://www.royalcaribbean.com" + sailings.getBookingLink());
    }

}