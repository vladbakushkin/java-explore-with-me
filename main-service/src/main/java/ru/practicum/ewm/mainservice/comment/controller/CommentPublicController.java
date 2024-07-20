package ru.practicum.ewm.mainservice.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.mainservice.comment.dto.CommentDto;
import ru.practicum.ewm.mainservice.comment.dto.CommentQueryParams;
import ru.practicum.ewm.mainservice.comment.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentPublicController {
    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getComments(CommentQueryParams queryParams,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("*----- public getting comments with params: {}, {}, {}", queryParams, from, size);
        return commentService.getComments(queryParams, from, size);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getComment(@PathVariable Long commentId) {
        log.info("*----- public getting comment with id {}", commentId);
        return commentService.getComment(commentId);
    }
}
