package com.example.superMalle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "menu_item_option_group")
public class MenuItemOptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    @ToString.Exclude
    private MenuItem menuItem;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRequired = false;

    @Builder.Default
    @Column(name = "max_selections")
    private Integer maxSelections = 1;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @OrderBy("sortOrder ASC")
    private List<MenuItemOption> options = new ArrayList<>();
}
