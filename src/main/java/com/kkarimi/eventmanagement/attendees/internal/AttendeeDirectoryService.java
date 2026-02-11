package com.kkarimi.eventmanagement.attendees.internal;

import com.kkarimi.eventmanagement.changeshipping.TrackDataChange;
import com.kkarimi.eventmanagement.attendees.Attendee;
import com.kkarimi.eventmanagement.attendees.AttendeeDirectory;
import com.kkarimi.eventmanagement.attendees.NewAttendeeCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class AttendeeDirectoryService implements AttendeeDirectory {

    private final AttendeeJpaRepository repository;
    private final AttendeeMapper mapper;

    @Override
    @Transactional
    @TrackDataChange(module = "attendees", action = "register", entity = "attendee")
    public Attendee register(NewAttendeeCommand command) {
        UUID id = UUID.randomUUID();
        AttendeeJpaEntity entity = mapper.toEntity(id, command);
        return mapper.toModel(repository.save(entity));
    }

    @Override
    public Optional<Attendee> findById(UUID attendeeId) {
        return repository.findById(attendeeId).map(mapper::toModel);
    }

    @Override
    public List<Attendee> findAll() {
        return repository.findAll().stream().map(mapper::toModel).toList();
    }
}
