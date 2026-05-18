package com.example.superMalle.service;

import com.example.superMalle.dto.cart.*;
import com.example.superMalle.entity.Cart;
import com.example.superMalle.entity.CartItem;
import com.example.superMalle.entity.MenuItem;
import com.example.superMalle.entity.User;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.CartItemRepository;
import com.example.superMalle.repository.CartRepository;
import com.example.superMalle.repository.MenuItemRepository;
import com.example.superMalle.repository.UserRepository;
import com.example.superMalle.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Value("${app.restaurant.tax-rate}")
    private double taxRate;

    public CartResponse getCart() {
        Cart cart = getOrCreateCart();
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        if (request.getMenuItemId() == null) {
            throw new BadRequestException("Menu item ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        if (request.getQuantity() > 50) {
            throw new BadRequestException("Cannot add more than 50 of the same item");
        }

        Cart cart = getOrCreateCart();
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", request.getMenuItemId()));

        if (!menuItem.getIsAvailable()) {
            throw new BadRequestException("Menu item is not available");
        }

        if (menuItem.getPrice() == null) {
            throw new BadRequestException("Menu item has no valid price");
        }

        // Check if item already exists in cart with same customizations
        var existingItem = (cart.getItems() != null ? cart.getItems() : List.<CartItem>of()).stream()
                .filter(ci -> ci.getMenuItem() != null && ci.getMenuItem().getId().equals(request.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > 50) {
                throw new BadRequestException("Total quantity for this item cannot exceed 50");
            }
            item.setQuantity(newQuantity);
            if (request.getCustomizations() != null) {
                item.setCustomizations(request.getCustomizations());
            }
            if (request.getSpecialInstructions() != null) {
                item.setSpecialInstructions(request.getSpecialInstructions());
            }
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .customizations(request.getCustomizations())
                    .specialInstructions(request.getSpecialInstructions())
                    .build();
            cart.getItems().add(cartItem);
        }

        cart = cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request) {
        if (itemId == null) {
            throw new BadRequestException("Cart item ID is required");
        }
        if (request.getQuantity() == null) {
            throw new BadRequestException("Quantity is required");
        }

        Cart cart = getOrCreateCart();

        CartItem cartItem = (cart.getItems() != null ? cart.getItems() : List.<CartItem>of()).stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(cartItem);
        } else if (request.getQuantity() > 50) {
            throw new BadRequestException("Quantity cannot exceed 50");
        } else {
            cartItem.setQuantity(request.getQuantity());
        }

        cart = cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(Long itemId) {
        if (itemId == null) {
            throw new BadRequestException("Cart item ID is required");
        }
        Cart cart = getOrCreateCart();
        if (cart.getItems() != null) {
            boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
            if (!removed) {
                throw new ResourceNotFoundException("CartItem", "id", itemId);
            }
        }
        cart = cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart() {
        Long userId = getAuthenticatedUserId();
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("No authenticated user found");
        }
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("Invalid authentication principal");
        }
        return userDetails.getId();
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItem> items = cart.getItems() != null ? cart.getItems() : List.of();

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(item -> item.getUnitPrice() != null ?
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) :
                        BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(taxRate));
        BigDecimal total = subtotal.add(tax);

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getUnitPrice() != null ?
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) :
                BigDecimal.ZERO;
        return CartItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .menuItemName(item.getMenuItem() != null ? item.getMenuItem().getName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .customizations(item.getCustomizations())
                .specialInstructions(item.getSpecialInstructions())
                .subtotal(subtotal)
                .build();
    }
}
