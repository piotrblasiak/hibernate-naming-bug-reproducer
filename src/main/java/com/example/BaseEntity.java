package com.example;

import jakarta.persistence.*;

/**
 * Base entity with an implicit @ManyToOne join column.
 * The join column name should be converted to snake_case: "last_modifier_id"
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * BUG: This implicit join column should be named "last_modifier_id" (snake_case).
     * But when User has a quoted @Table name, Hibernate 7 generates "lastModifier_id"
     * (camelCase, quoted), bypassing the PhysicalNamingStrategy.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private User lastModifier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(User lastModifier) {
        this.lastModifier = lastModifier;
    }
}
