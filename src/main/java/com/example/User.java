package com.example;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Entity with a quoted table name to handle SQL reserved word "user".
 *
 * THE BUG: Using backticks to quote the table name causes Hibernate 7 to
 * propagate the quoting to ALL implicit identifiers that reference this entity,
 * bypassing the PhysicalNamingStrategy (CamelCaseToUnderscoresNamingStrategy).
 */
@Entity
@Table(name = "`user`") // Quoted because "user" is a SQL reserved word
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    /**
     * ElementCollection table should be: user_user_roles (snake_case)
     * Buggy behavior generates: "User_userRoles" (quoted, camelCase)
     */
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
