package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @ToString.Exclude
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    @ToString.Exclude
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 4)
    private BigDecimal unitPrice;

    @Column(columnDefinition = "TEXT")
    private String customizations;

    private String specialInstructions;
}
