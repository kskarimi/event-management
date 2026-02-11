package com.kkarimi.eventmanagement.attendees.internal;

import com.kkarimi.eventmanagement.attendees.Attendee;
import com.kkarimi.eventmanagement.attendees.NewAttendeeCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
interface AttendeeMapper {

    Attendee toModel(AttendeeJpaEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "command.fullName")
    @Mapping(target = "email", source = "command.email")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AttendeeJpaEntity toEntity(UUID id, NewAttendeeCommand command);
}
