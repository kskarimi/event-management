package com.kkarimi.eventmanagement.registration.internal;

import com.kkarimi.eventmanagement.registration.Registration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface RegistrationMapper {

    Registration toModel(RegistrationJpaEntity entity);

    RegistrationJpaEntity toEntity(Registration registration);
}
