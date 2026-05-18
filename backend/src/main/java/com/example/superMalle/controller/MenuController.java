package com.example.superMalle.controller;

import com.example.superMalle.dto.menu.MenuItemResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<PagedResponse<MenuItemResponse>> getMenuItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(menuItemService.getAvailableMenuItems(categoryId, search, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }
}
