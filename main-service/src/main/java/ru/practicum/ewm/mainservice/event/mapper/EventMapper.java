package ru.practicum.ewm.mainservice.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import ru.practicum.ewm.mainservice.event.dto.EventFullDto;
import ru.practicum.ewm.mainservice.event.dto.EventShortDto;
import ru.practicum.ewm.mainservice.event.dto.NewEventDto;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.event.model.Location;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {
    @Mapping(target = "category", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "location", qualifiedByName = "getLocation", source = "event")
    EventFullDto toEventFullDto(Event event);

    List<EventFullDto> toEventFullDtoList(List<Event> events);

    List<EventShortDto> toEventShortDtoList(List<Event> events);

    @Named("getLocation")
    default Location getLocation(Event event) {
        return new Location(event.getLat(), event.getLon());
    }
}
