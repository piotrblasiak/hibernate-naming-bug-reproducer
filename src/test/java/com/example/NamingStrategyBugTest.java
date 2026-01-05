package com.example;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests demonstrating the Hibernate 7 naming strategy bug.
 *
 * When an entity uses a quoted @Table name (e.g., @Table(name = "`user`")),
 * the PhysicalNamingStrategy is NOT applied to:
 * 1. Implicit @ManyToOne join column names
 * 2. @ElementCollection table names
 *
 * These tests pass when using the workaround (QuotedIdentifierNamingStrategy)
 * but would fail with the default CamelCaseToUnderscoresNamingStrategy.
 *
 * To reproduce the bug, change application.yml to use:
 *   physical-strategy: org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
 */
@SpringBootTest
@Transactional
class NamingStrategyBugTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void regularColumnsUseSnakeCase() {
        // Regular columns work correctly with snake_case
        var result = entityManager.createNativeQuery(
                "SELECT post_title, post_content FROM post WHERE id = 1"
        ).getResultList();
        assertThat(result).isNotNull();
    }

    @Test
    void joinColumnsShouldUseSnakeCase() {
        // Join columns should be: created_by_id, last_modified_by_id
        // Bug generates: "createdBy_id", "lastModifiedBy_id" (quoted camelCase)
        var result = entityManager.createNativeQuery(
                "SELECT id, post_title, created_by_id, last_modified_by_id FROM post WHERE id = 1"
        ).getResultList();
        assertThat(result).isNotNull();
    }

    @Test
    void elementCollectionTableShouldUseSnakeCase() {
        // Table should be: user_user_roles
        // Bug generates: "User_userRoles" (quoted camelCase)
        var result = entityManager.createNativeQuery(
                "SELECT * FROM user_user_roles"
        ).getResultList();
        assertThat(result).isNotNull();
    }

    @Test
    void persistAndQueryWithJoinColumn() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserRoles(Set.of(User.Role.USER));
        entityManager.persist(user);

        Post post = new Post();
        post.setPostTitle("Test Post");
        post.setPostContent("Content");
        post.setCreatedBy(user);
        post.setLastModifiedBy(user);
        entityManager.persist(post);

        entityManager.flush();
        entityManager.clear();

        // Check console output for generated SQL column names
        var loadedPost = entityManager.createQuery(
                "SELECT p FROM Post p JOIN FETCH p.createdBy WHERE p.id = :id", Post.class
        ).setParameter("id", post.getId()).getSingleResult();

        assertThat(loadedPost.getCreatedBy().getFirstName()).isEqualTo("John");
    }
}
