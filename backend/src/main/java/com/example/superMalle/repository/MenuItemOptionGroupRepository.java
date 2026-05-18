package com.example.superMalle.repository;

import com.example.superMalle.entity.MenuItemOptionGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemOptionGroupRepository extends JpaRepository<MenuItemOptionGroup, Long> {
    @EntityGraph(attributePaths = "options")
    List<MenuItemOptionGroup> findByMenuItemIdOrderBySortOrder(Long menuItemId);

    @EntityGraph(attributePaths = "options")
    List<MenuItemOptionGroup> findByMenuItemIdInOrderBySortOrder(List<Long> menuItemIds);

    void deleteByMenuItemId(Long menuItemId);
}
