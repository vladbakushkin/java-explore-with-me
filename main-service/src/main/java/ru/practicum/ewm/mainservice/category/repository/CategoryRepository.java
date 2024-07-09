package ru.practicum.ewm.mainservice.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.mainservice.category.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
