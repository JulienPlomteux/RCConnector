package com.plomteux.rcconnector.mapper;

import com.plomteux.rcconnector.entity.DestinationCodeEntity;
import com.plomteux.rcconnector.model.CruiseMasterSailingItineraryDaysInnerPortsInnerPort;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface DestinationCodeMapper {
    DestinationCodeMapper INSTANCE = Mappers.getMapper(DestinationCodeMapper.class);

    DestinationCodeEntity toDestinationCodeEntity(CruiseMasterSailingItineraryDaysInnerPortsInnerPort port);
}
