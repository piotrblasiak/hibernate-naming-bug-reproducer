package com.example;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Entity using a SQL reserved word "user" as the table name.
 *
 * This demonstrates TWO related bugs where PhysicalNamingStrategy is bypassed:
 *
 * BUG 1: When using hibernate.auto_quote_keyword=true (no @Table annotation needed)
 *        - Table becomes "User" instead of "user" (no snake_case applied)
 *        - Join columns become "createdBy_id" instead of "created_by_id"
 *
 * BUG 2: When manually quoting with @Table(name = "`user`")
 *        - Table is correctly "user" (we specified it)
 *        - But join columns still become "createdBy_id" instead of "created_by_id"
 *
 * See application.yml to switch between test scenarios.
 */
@Entity
// For BUG 1 test: Comment out @Table, enable auto_quote_keyword=true in application.yml
// For BUG 2 test: Uncomment @Table, disable auto_quote_keyword in application.yml
@Table(name = "`user`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<Role> userRoles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Role> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<Role> userRoles) {
        this.userRoles = userRoles;
    }

    public enum Role {
        ADMIN, USER
    }
}
