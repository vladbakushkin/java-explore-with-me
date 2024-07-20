package ru.practicum.ewm.mainservice.comment.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mainservice.comment.dto.CommentDto;
import ru.practicum.ewm.mainservice.comment.dto.CommentQueryParams;
import ru.practicum.ewm.mainservice.comment.dto.NewCommentDto;
import ru.practicum.ewm.mainservice.comment.mapper.CommentMapper;
import ru.practicum.ewm.mainservice.comment.model.Comment;
import ru.practicum.ewm.mainservice.comment.model.QComment;
import ru.practicum.ewm.mainservice.comment.repository.CommentRepository;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.event.model.state.EventState;
import ru.practicum.ewm.mainservice.event.repository.EventRepository;
import ru.practicum.ewm.mainservice.exception.BadRequestException;
import ru.practicum.ewm.mainservice.exception.ConflictException;
import ru.practicum.ewm.mainservice.exception.NotFoundException;
import ru.practicum.ewm.mainservice.user.model.User;
import ru.practicum.ewm.mainservice.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long userId, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

        List<Comment> comments = commentRepository.findAllByAuthorId(author.getId(), pageable);

        List<CommentDto> commentDtoList = commentMapper.toCommentDtoList(comments);
        log.info("#----- private got comments {}", commentDtoList);
        return commentDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(CommentQueryParams queryParams, Integer from, Integer size) {
        QComment comment = QComment.comment;

        BooleanBuilder params = new BooleanBuilder();

        String text = queryParams.getText();
        if (text != null) {
            params.and(comment.text.containsIgnoreCase(text));
        }

        Long event = queryParams.getEvent();
        if (event != null) {
            params.and(comment.event.id.eq(event));
        }

        Long author = queryParams.getAuthor();
        if (author != null) {
            params.and(comment.author.id.eq(author));
        }

        LocalDateTime rangeStart = queryParams.getRangeStart();
        LocalDateTime rangeEnd = queryParams.getRangeEnd();
        if (rangeStart != null) {
            params.and(comment.createdOn.after(rangeStart));
        }
        if (queryParams.getRangeEnd() != null) {
            params.and(comment.createdOn.before(rangeEnd));
        }
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestException("End time must be after start time");
            }
            params.and(comment.createdOn.between(rangeStart, rangeEnd));
        }

        OrderSpecifier<LocalDateTime> sort = queryParams.getSort().equals("DESC") ?
                comment.createdOn.desc() : comment.createdOn.asc();

        List<Comment> comments = queryFactory.selectFrom(comment)
                .where(params)
                .orderBy(sort)
                .offset(from)
                .limit(size)
                .fetch();

        List<CommentDto> commentDtoList = commentMapper.toCommentDtoList(comments);
        log.info("#----- public got comments {}", commentDtoList);
        return commentDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " not found"));

        CommentDto commentDto = commentMapper.toCommentDto(comment);
        log.info("#----- got comment {}", commentDto);
        return commentDto;
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("You cannot comment in an unpublished event");
        }

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);
        Comment savedComment = commentRepository.save(comment);

        CommentDto commentDto = commentMapper.toCommentDto(savedComment);
        log.info("#----- saved comment {}", commentDto);
        return commentDto;
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " not found");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " not found"));

        comment.setText(newCommentDto.getText());
        Comment savedComment = commentRepository.save(comment);

        CommentDto commentDto = commentMapper.toCommentDto(savedComment);
        log.info("#----- updated comment {}", commentDto);
        return commentDto;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " not found"));

        if (!Objects.equals(author.getId(), comment.getAuthor().getId())) {
            throw new ConflictException("Only the author can delete this comment");
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
