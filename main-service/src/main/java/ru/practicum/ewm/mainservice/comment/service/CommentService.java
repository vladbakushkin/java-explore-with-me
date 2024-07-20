package ru.practicum.ewm.mainservice.comment.service;

import ru.practicum.ewm.mainservice.comment.dto.CommentDto;
import ru.practicum.ewm.mainservice.comment.dto.CommentQueryParams;
import ru.practicum.ewm.mainservice.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> getComments(Long userId, Integer from, Integer size);

    List<CommentDto> getComments(CommentQueryParams queryParams, Integer from, Integer size);

    CommentDto getComment(Long commentId);

    CommentDto addComment(Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto);

    void deleteComment(Long userId, Long commentId);

    void deleteComment(Long commentId);
}
