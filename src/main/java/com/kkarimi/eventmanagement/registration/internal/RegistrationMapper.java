package com.kkarimi.eventmanagement.registration.internal;

import com.kkarimi.eventmanagement.registration.Registration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface RegistrationMapper {

    Registration toModel(RegistrationJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RegistrationJpaEntity toEntity(Registration registration);
}
