package com.example.superMalle.repository;

import com.example.superMalle.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @EntityGraph(attributePaths = "menuItems")
    List<Category> findByIsActiveTrueOrderBySortOrder();

    @EntityGraph(attributePaths = "menuItems")
    List<Category> findAllByOrderBySortOrder();
}
