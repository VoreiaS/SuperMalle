package com.example.superMalle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.superMalle.entity.enums.Allergen;
import com.example.superMalle.entity.enums.DietaryTag;
import com.example.superMalle.entity.enums.TaxCategory;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_category", columnDefinition = "VARCHAR(20)")
    @Builder.Default
    private TaxCategory taxCategory = TaxCategory.STANDARD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    @Pattern(
        regexp = "^https://[\\w.-]+\\.[\\w]{2,}(/[\\w./?%&=-]*)?\\.(jpg|jpeg|png|webp|gif)$",
        message = "Image URL must be HTTPS with valid image extension (jpg/png/webp/gif)"
    )
    @Column(length = 500)
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Builder.Default
    private Integer preparationTimeMinutes = 15;

    @Column(columnDefinition = "TEXT")
    private String customizations;

    @ElementCollection(targetClass = Allergen.class)
    @CollectionTable(name = "menu_item_allergens", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "allergen")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<Allergen> allergens = new ArrayList<>();

    @ElementCollection(targetClass = DietaryTag.class)
    @CollectionTable(name = "menu_item_dietary_tags", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "dietary_tag")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<DietaryTag> dietaryTags = new ArrayList<>();

    /** Optimistic locking version for concurrency control - prevents oversell race conditions */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /** Soft-delete support */
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    @Transient
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    @PrePersist
    @PreUpdate
    public void handleSoftDelete() {
        if (Boolean.TRUE.equals(deleted) && deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }
}
