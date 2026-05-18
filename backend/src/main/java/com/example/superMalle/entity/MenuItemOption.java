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
@Table(name = "menu_item_option")
public class MenuItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false)
    @ToString.Exclude
    private MenuItemOptionGroup optionGroup;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_modifier", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal priceModifier = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
}
