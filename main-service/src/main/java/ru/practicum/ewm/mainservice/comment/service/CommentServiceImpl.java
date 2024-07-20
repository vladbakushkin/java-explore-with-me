package ru.practicum.ewm.mainservice.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.mainservice.comment.dto.CommentDto;
import ru.practicum.ewm.mainservice.comment.dto.CommentQueryParams;
import ru.practicum.ewm.mainservice.comment.dto.NewCommentDto;
import ru.practicum.ewm.mainservice.comment.mapper.CommentMapper;
import ru.practicum.ewm.mainservice.comment.repository.CommentRepository;
import ru.practicum.ewm.mainservice.event.repository.EventRepository;
import ru.practicum.ewm.mainservice.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentDto> getComments(Long userId, Integer from, Integer size) {
        return List.of();
    }

    @Override
    public List<CommentDto> getComments(CommentQueryParams queryParams, Integer from, Integer size) {
        return List.of();
    }

    @Override
    public CommentDto getComment(Long commentId) {
        return null;
    }

    @Override
    public CommentDto addComment(Long userId, NewCommentDto newCommentDto) {
        return null;
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto) {
        return null;
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {

    }

    @Override
    public void deleteComment(Long commentId) {

    }
}
