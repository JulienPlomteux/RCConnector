package com.plomteux.rcconnector.mapper;

import com.plomteux.rcconnector.entity.CruiseDetailsEntity;
import com.plomteux.rcconnector.entity.DestinationCodeEntity;
import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.model.CruiseOverView;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {SailingsMapper.class, CruiseDetailsMapper.class, CruiseOverViewMapper.class})
public interface CruiseOverViewMapper {

    @Mapping(target = "productViewLink", source = "sailingsEntity.bookingLink")
    CruiseOverView toCruiseOverView(SailingsEntity sailingsEntity, BigDecimal oldPrice);

    @AfterMapping
    default void addedMapping(SailingsEntity sailingsEntity, BigDecimal priceDrop, @MappingTarget CruiseOverView cruiseOverView) {
        cruiseOverView.setPriceDrop(priceDrop);
        CruiseDetailsEntity cruiseDetailsEntity = sailingsEntity.getCruiseDetailsEntity();
        cruiseOverView.setPortsOfCall(
                cruiseDetailsEntity.getDestinationsEntities().stream()
                        .map(DestinationCodeEntity::getName)
                        .toList()
        );
        cruiseOverView.setDuration(cruiseDetailsEntity.getDuration());
        cruiseOverView.setEmbarkationPort(cruiseDetailsEntity.getEmbarkationPortCode());
    }



}
