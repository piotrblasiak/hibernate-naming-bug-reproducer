# Hibernate 7 PhysicalNamingStrategy Bug Reproducer

## Problem

When an entity uses a **quoted `@Table` name** (e.g., `` @Table(name = "`user`") `` to handle SQL reserved words), Hibernate 7 propagates the quoting to **all implicit identifiers** that reference this entity, **bypassing the `PhysicalNamingStrategy`**.

This affects:
- Implicit `@ManyToOne` join column names
- `@ElementCollection` table names
- Any other implicit identifier derived from the quoted entity

## Environment

- **Hibernate ORM:** 7.2.0.Final
- **Spring Boot:** 4.0.1
- **Java:** 21

## Expected vs Actual Behavior

### Join Columns

| Field | Expected Column Name | Actual (Buggy) Name |
|-------|---------------------|---------------------|
| `createdBy` | `created_by_id` | `"createdBy_id"` |
| `lastModifiedBy` | `last_modified_by_id` | `"lastModifiedBy_id"` |
| `lastModifier` | `last_modifier_id` | `"lastModifier_id"` |

### Element Collection Tables

| Field | Expected Table Name | Actual (Buggy) Name |
|-------|---------------------|---------------------|
| `userRoles` | `user_user_roles` | `"User_userRoles"` |

## Root Cause

When you use backticks in `@Table(name = "`user`")`:
1. Hibernate marks the table identifier as "quoted"
2. This quoted status propagates to implicit identifiers (join columns, collection tables)
3. Quoted identifiers bypass `CamelCaseToUnderscoresNamingStrategy`
4. The identifiers remain in camelCase and are quoted in SQL

## Reproducing the Bug

1. Clone this repository
2. Run tests - they will **FAIL**:

```bash
mvn test
```

3. Check the console output - you'll see quoted camelCase column names like `"createdBy_id"`

## Workaround

Edit `src/main/resources/application.yml` to use the custom `QuotedIdentifierNamingStrategy`:

```yaml
spring:
  jpa:
    hibernate:
      naming:
        #physical-strategy: org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
        physical-strategy: com.example.QuotedIdentifierNamingStrategy
```

This strategy:
1. Applies snake_case conversion regardless of quoting status
2. Removes unnecessary quoting from column names
3. Only preserves quoting for table names that are SQL reserved words

Run tests again - they will **PASS**:

```bash
mvn test
```

## Project Structure

```
src/main/java/com/example/
├── Application.java                     # Spring Boot application
├── User.java                            # Entity with quoted @Table (the trigger)
├── Post.java                            # Entity with @ManyToOne to User
├── BaseEntity.java                      # @MappedSuperclass with @ManyToOne to User
└── QuotedIdentifierNamingStrategy.java  # Workaround implementation

src/test/java/com/example/
└── NamingStrategyBugTest.java           # Tests demonstrating the issue
```

## Impact

This bug causes issues with **PostgreSQL** and other databases that treat quoted identifiers as case-sensitive. Queries fail with errors like:

```
ERROR: column "lastModifier_id" does not exist
Hint: Perhaps you meant to reference the column "last_modifier_id"
```

## Analysis

The `PhysicalNamingStrategy` is bypassed for identifiers that are marked as "quoted". When an entity has a quoted table name, this quoting propagates to derived identifiers (join columns, collection tables), and the physical naming strategy never gets a chance to convert them to snake_case.
