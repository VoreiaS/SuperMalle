package com.example.superMalle.repository;

import com.example.superMalle.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    void deleteByCartIdAndId(Long cartId, Long id);
    void deleteAllByCartId(Long cartId);
}
