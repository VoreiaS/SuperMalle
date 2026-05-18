package com.example.superMalle.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    @ToString.Exclude
    private MenuItem menuItem;

    @Column(nullable = false)
    private String menuItemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 4)
    private BigDecimal unitPrice;

    @Column(columnDefinition = "TEXT")
    private String customizations;

    @Column(precision = 12, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "option_names", columnDefinition = "TEXT")
    private String optionNames;

    @Column(name = "option_price_adjustment", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal optionPriceAdjustment = BigDecimal.ZERO;
}
