package com.plomteux.rcconnector.mapper;

import com.plomteux.rcconnector.entity.CruiseDetailsEntity;
import com.plomteux.rcconnector.entity.DestinationCodeEntity;
import com.plomteux.rcconnector.entity.SailingsEntity;
import com.plomteux.rcconnector.model.Cruise;
import com.plomteux.rcconnector.model.CruiseMasterSailingItineraryDaysInner;
import com.plomteux.rcconnector.model.CruiseMasterSailingItineraryDaysInnerPortsInner;
import com.plomteux.rcconnector.model.Sailings;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SailingsMapper.class, DestinationCodeMapper.class, CruiseDetailsMapper.class})
public interface CruiseDetailsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "id")
    @Mapping(target = "duration", source = "masterSailing.itinerary.totalNights")
    @Mapping(target = "embarkationPortCode", source = "masterSailing.itinerary.departurePort.name")
    @Mapping(target = "destination", source = "masterSailing.itinerary.destination.name")
    @Mapping(target = "destinationsEntities", source = "masterSailing.itinerary.days", qualifiedByName = "mapPortsToDestinations")
    @Mapping(target = "sailingsEntities", source = "sailings")
    CruiseDetailsEntity toCruiseDetailsEntity(Cruise cruiseDetails);

    @Mapping(target = "sailings", source = "sailingsEntities")
    Cruise toCruise(CruiseDetailsEntity cruiseDetailsEntity);

    List<SailingsEntity> toSailingsEntities(List<Sailings> sailingsList);

    @Named("mapPortsToDestinations")
    default List<DestinationCodeEntity> mapPortsToDestinations(List<CruiseMasterSailingItineraryDaysInner> days) {
        return days.stream()
                .flatMap(daysInner -> daysInner.getPorts().stream())
                .map(CruiseMasterSailingItineraryDaysInnerPortsInner::getPort)
                .map(DestinationCodeMapper.INSTANCE::toDestinationCodeEntity)
                .toList();
    }

    @AfterMapping
    default void addedMapping(Cruise cruise, @MappingTarget CruiseDetailsEntity cruiseDetailsEntity){
        cruiseDetailsEntity.getSailingsEntities().forEach(sailingsEntity -> sailingsEntity.setCruiseDetailsEntity(cruiseDetailsEntity));
        cruiseDetailsEntity.getDestinationsEntities().forEach(destinationCodeEntity -> destinationCodeEntity.setCruiseDetailsEntity(cruiseDetailsEntity));
    }
}