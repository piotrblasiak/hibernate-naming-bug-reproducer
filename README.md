# Hibernate 7 PhysicalNamingStrategy Bug Reproducer

## Problem

Quoted identifiers bypass `PhysicalNamingStrategy` (e.g., `CamelCaseToUnderscoresNamingStrategy`), causing incorrect column and table names.

This manifests in **two related scenarios**:

### Bug 1: `hibernate.auto_quote_keyword=true`

When using auto-quoting for SQL reserved words, the `PhysicalNamingStrategy` is completely bypassed:

| Identifier | Expected | Actual |
|------------|----------|--------|
| Table `User` | `"user"` | `"User"` |
| Join column `createdBy` | `created_by_id` | `"createdBy_id"` |
| Collection table | `user_user_roles` | `"User_userRoles"` |

### Bug 2: Manual quoting with `@Table(name = "`user`")`

When manually quoting a table name, the quoting propagates to all derived identifiers:

| Identifier | Expected | Actual |
|------------|----------|--------|
| Table | `"user"` ✓ | `"user"` ✓ |
| Join column `createdBy` | `created_by_id` | `"createdBy_id"` |
| Collection table | `user_user_roles` | `"user_user_roles"` ✓ |

## Environment

- **Hibernate ORM:** 7.2.0.Final
- **Spring Boot:** 4.0.1
- **Java:** 21

## Reproducing Bug 1 (auto_quote_keyword)

1. Edit `src/main/java/com/example/User.java` - comment out `@Table(name = "`user`")`
2. Edit `src/main/resources/application.yml` - add `hibernate.auto_quote_keyword: true`
3. Run tests:

```bash
mvn test
```

Tests will **FAIL**. Check console output for wrong column names like `"createdBy_id"`.

## Reproducing Bug 2 (manual backticks)

This is the default configuration:

1. Ensure `@Table(name = "`user`")` is present in `User.java`
2. Ensure `auto_quote_keyword` is NOT set in `application.yml`
3. Run tests:

```bash
mvn test
```

Tests will **FAIL**. Check console output for wrong column names like `"createdBy_id"`.

## Root Cause

When an identifier is marked as "quoted" (either via `auto_quote_keyword` or manual backticks):
1. Hibernate preserves the quoted flag on derived identifiers (join columns, collection tables)
2. The `PhysicalNamingStrategy` does not process quoted identifiers
3. CamelCase names remain unchanged instead of being converted to snake_case

## Workaround

Use the custom `QuotedIdentifierNamingStrategy` included in this project:

```yaml
spring:
  jpa:
    hibernate:
      naming:
        physical-strategy: com.example.QuotedIdentifierNamingStrategy
```

This strategy:
1. Forces snake_case conversion regardless of quoting status
2. Removes unnecessary quoting from column names
3. Only preserves quoting for table names that are SQL reserved words

## Project Structure

```
src/main/java/com/example/
├── Application.java                     # Spring Boot application
├── User.java                            # Entity with reserved word table name
├── Post.java                            # Entity with @ManyToOne to User
├── BaseEntity.java                      # @MappedSuperclass with @ManyToOne to User
└── QuotedIdentifierNamingStrategy.java  # Workaround implementation

src/test/java/com/example/
└── NamingStrategyBugTest.java           # Tests demonstrating the issue
```

## Impact

This bug causes issues with **PostgreSQL** and other databases that treat quoted identifiers as case-sensitive. Queries fail with errors like:

```
ERROR: column "createdBy_id" does not exist
Hint: Perhaps you meant to reference the column "created_by_id"
```
