package com.example;

import jakarta.persistence.*;

/**
 * Entity with multiple @ManyToOne relationships to User (which has a quoted table name).
 * All join columns should be snake_case but are incorrectly generated as quoted camelCase.
 */
@Entity
public class Post extends BaseEntity {

    private String postTitle;

    private String postContent;

    /**
     * Expected column name: created_by_id
     * Actual (buggy) name: "createdBy_id" (quoted, camelCase)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    /**
     * Expected column name: last_modified_by_id
     * Actual (buggy) name: "lastModifiedBy_id" (quoted, camelCase)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private User lastModifiedBy;

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
