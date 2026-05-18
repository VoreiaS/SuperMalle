package com.example.superMalle.service;

import com.example.superMalle.config.CacheConfig;
import com.example.superMalle.dto.menu.MenuItemRequest;
import com.example.superMalle.dto.menu.MenuItemResponse;
import com.example.superMalle.dto.menu.OptionGroupResponse;
import com.example.superMalle.dto.menu.OptionResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.Category;
import com.example.superMalle.entity.MenuItem;
import com.example.superMalle.entity.MenuItemOptionGroup;
import com.example.superMalle.entity.enums.TaxCategory;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.CategoryRepository;
import com.example.superMalle.repository.MenuItemOptionGroupRepository;
import com.example.superMalle.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemOptionGroupRepository menuItemOptionGroupRepository;

    @Cacheable(value = CacheConfig.MENU_ITEMS_CACHE, key = "'available:' + #categoryId + ':' + #search + ':' + #page + ':' + #size")
    public PagedResponse<MenuItemResponse> getAvailableMenuItems(Long categoryId, String search, int page, int size) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<MenuItem> menuItemPage;

        if (search != null && !search.isBlank() && categoryId != null) {
            menuItemPage = menuItemRepository.searchAvailableItemsByCategory(categoryId, search, pageable);
        } else if (search != null && !search.isBlank()) {
            menuItemPage = menuItemRepository.searchAvailableItems(search, pageable);
        } else if (categoryId != null) {
            menuItemPage = menuItemRepository.findByCategoryIdAndIsAvailableTrue(categoryId, pageable);
        } else {
            menuItemPage = menuItemRepository.findByIsAvailableTrue(pageable);
        }

        List<MenuItem> items = menuItemPage.getContent();
        Map<Long, List<OptionGroupResponse>> optionGroupsByItemId = batchFetchOptionGroups(items);

        return PagedResponse.<MenuItemResponse>builder()
                .items(items.stream().map(item -> toResponse(item, optionGroupsByItemId.get(item.getId()))).toList())
                .total(menuItemPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(menuItemPage.getTotalPages())
                .build();
    }

    @Cacheable(value = CacheConfig.MENU_ITEMS_CACHE, key = "'item:' + #id")
    public MenuItemResponse getMenuItemById(Long id) {
        if (id == null) {
            throw new BadRequestException("Menu item ID is required");
        }
        MenuItem item = menuItemRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return toResponse(item);
    }

    public PagedResponse<MenuItemResponse> getAllMenuItems(int page, int size,
                                                            String search, Long categoryId) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MenuItem> menuItemPage;
        if (search != null || categoryId != null) {
            menuItemPage = menuItemRepository.adminSearch(search, categoryId, pageable);
        } else {
            menuItemPage = menuItemRepository.findAllWithCategory(pageable);
        }

        List<MenuItem> items = menuItemPage.getContent();
        Map<Long, List<OptionGroupResponse>> optionGroupsByItemId = batchFetchOptionGroups(items);

        return PagedResponse.<MenuItemResponse>builder()
                .items(items.stream().map(item -> toResponse(item, optionGroupsByItemId.get(item.getId()))).toList())
                .total(menuItemPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(menuItemPage.getTotalPages())
                .build();
    }

    @Transactional
    @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        if (request == null) {
            throw new BadRequestException("Menu item request cannot be null");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Menu item name is required");
        }
        if (request.getPrice() == null) {
            throw new BadRequestException("Menu item price is required");
        }
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Menu item price must be greater than zero");
        }
        if (request.getCategoryId() == null) {
            throw new BadRequestException("Category ID is required");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .preparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : 15)
                .customizations(request.getCustomizations())
                .taxCategory(request.getTaxCategory() != null ? request.getTaxCategory() : TaxCategory.STANDARD)
                .build();

        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, key = "'item:' + #id"),
        @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    })
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        if (id == null) {
            throw new BadRequestException("Menu item ID is required");
        }
        if (request == null) {
            throw new BadRequestException("Menu item request cannot be null");
        }

        MenuItem item = menuItemRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Price must be greater than zero");
            }
            item.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            item.setCategory(category);
        }
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());
        if (request.getPreparationTimeMinutes() != null) {
            if (request.getPreparationTimeMinutes() < 1) {
                throw new BadRequestException("Preparation time must be at least 1 minute");
            }
            item.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        }
        if (request.getCustomizations() != null) item.setCustomizations(request.getCustomizations());
        if (request.getTaxCategory() != null) item.setTaxCategory(request.getTaxCategory());

        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, key = "'item:' + #id"),
        @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    })
    public void deleteMenuItem(Long id) {
        if (id == null) {
            throw new BadRequestException("Menu item ID is required");
        }
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        item.setIsAvailable(false);
        menuItemRepository.save(item);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, key = "'item:' + #id"),
        @CacheEvict(value = CacheConfig.MENU_ITEMS_CACHE, allEntries = true)
    })
    public MenuItemResponse toggleAvailability(Long id) {
        if (id == null) {
            throw new BadRequestException("Menu item ID is required");
        }
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        item.setIsAvailable(!item.getIsAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return toResponse(item, null);
    }

    private MenuItemResponse toResponse(MenuItem item, List<OptionGroupResponse> optionGroups) {
        if (optionGroups == null) {
            optionGroups = menuItemOptionGroupRepository
                    .findByMenuItemIdOrderBySortOrder(item.getId())
                    .stream()
                    .map(this::toOptionGroupResponse)
                    .toList();
        }

        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .imageUrl(item.getImageUrl())
                .isAvailable(item.getIsAvailable())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .customizations(item.getCustomizations())
                .taxCategory(item.getTaxCategory())
                .optionGroups(optionGroups)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private OptionGroupResponse toOptionGroupResponse(MenuItemOptionGroup g) {
        return OptionGroupResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .isRequired(g.getIsRequired())
                .maxSelections(g.getMaxSelections())
                .sortOrder(g.getSortOrder())
                .options(g.getOptions().stream()
                        .map(o -> OptionResponse.builder()
                                .id(o.getId())
                                .optionGroupId(g.getId())
                                .name(o.getName())
                                .priceModifier(o.getPriceModifier())
                                .isDefault(o.getIsDefault())
                                .sortOrder(o.getSortOrder())
                                .isActive(o.getIsActive())
                                .build())
                        .toList())
                .build();
    }

    private Map<Long, List<OptionGroupResponse>> batchFetchOptionGroups(List<MenuItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyMap();
        List<Long> itemIds = items.stream().map(MenuItem::getId).toList();
        List<MenuItemOptionGroup> allGroups = menuItemOptionGroupRepository
                .findByMenuItemIdInOrderBySortOrder(itemIds);
        return allGroups.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getMenuItem().getId(),
                        Collectors.mapping(this::toOptionGroupResponse, Collectors.toList())
                ));
    }
}
