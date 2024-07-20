package ru.practicum.ewm.mainservice.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mainservice.compilation.dto.CompilationDto;
import ru.practicum.ewm.mainservice.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.mainservice.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.mainservice.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.mainservice.compilation.model.Compilation;
import ru.practicum.ewm.mainservice.compilation.repository.CompilationRepository;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.event.repository.EventRepository;
import ru.practicum.ewm.mainservice.exception.NotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = compilationMapper.toCompilationDto(savedCompilation);
        log.info("#----- admin saved compilation: {}", compilationDto);
        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
        log.info("#----- admin deleted compilation: {}", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NoSuchElementException("No compilation found with id: " + compId));

        List<Long> eventIds = updateCompilationRequest.getEvents();
        if (eventIds != null && !eventIds.isEmpty()) {
            Collection<Event> events = eventRepository.findAllById(eventIds);
            compilation.setEvents(new HashSet<>(events));
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null && !updateCompilationRequest.getTitle().isEmpty()) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = compilationMapper.toCompilationDto(savedCompilation);
        log.info("#----- admin updated compilation: {}", compilationDto);
        return compilationDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        if (pinned != null) {
            List<Compilation> allByPinned = compilationRepository.findAllByPinned(pinned, pageable);
            List<CompilationDto> compilationDtoList = compilationMapper.toCompilationDtoList(allByPinned);
            log.info("#----- public get compilations: {}", compilationDtoList);
            return compilationDtoList;
        }

        Page<Compilation> allByPinned = compilationRepository.findAll(pageable);
        List<CompilationDto> compilationDtoList = compilationMapper.toCompilationDtoList(allByPinned);
        log.info("#----- public get compilations: {}", compilationDtoList);
        return compilationDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id = " + compId + " not found"));
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        log.info("#----- public get compilation: {}", compilationDto);
        return compilationDto;
    }
}
