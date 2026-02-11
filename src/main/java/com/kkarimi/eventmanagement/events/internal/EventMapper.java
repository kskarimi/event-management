package com.kkarimi.eventmanagement.events.internal;

import com.kkarimi.eventmanagement.events.Event;
import com.kkarimi.eventmanagement.events.NewEventCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
interface EventMapper {

    Event toModel(EventJpaEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "command.title")
    @Mapping(target = "startsAt", source = "command.startsAt")
    @Mapping(target = "capacity", source = "command.capacity")
    @Mapping(target = "reservedSeats", constant = "0")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventJpaEntity toEntity(UUID id, NewEventCommand command);
}
