package ru.practicum.ewm.mainservice.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.mainservice.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
