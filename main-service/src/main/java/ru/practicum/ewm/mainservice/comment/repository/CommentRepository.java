package ru.practicum.ewm.mainservice.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.mainservice.comment.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
