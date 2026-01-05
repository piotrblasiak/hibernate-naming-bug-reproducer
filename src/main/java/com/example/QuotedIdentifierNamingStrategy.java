package com.example;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Custom naming strategy that applies snake_case conversion to ALL identifiers,
 * including those that are quoted (e.g., from @Table(name = "`user`")).
 *
 * In Hibernate 7, when a table name is quoted using backticks, the quoting
 * propagates to implicit join column names, causing them to bypass the
 * physical naming strategy. This class forces the conversion to happen
 * regardless of quoting status.
 *
 * Additionally, this strategy REMOVES quoting from column names since columns
 * don't need quoting (only table names with reserved words need it).
 */
public class QuotedIdentifierNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

	@Override
	public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		if (logicalName == null) {
			return null;
		}
		// Apply snake_case conversion and REMOVE quoting from columns
		// Columns don't need quoting - only table names with reserved words do
		return super.toPhysicalColumnName(
			Identifier.toIdentifier(logicalName.getText(), false),
			jdbcEnvironment
		);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
		if (logicalName == null) {
			return null;
		}
		// Apply snake_case conversion
		Identifier converted = super.toPhysicalTableName(
			Identifier.toIdentifier(logicalName.getText(), false),
			jdbcEnvironment
		);
		// Only preserve quoting for tables that are SQL reserved words
		// For element collection tables and other derived tables, remove quoting
		String tableName = converted.getText();
		boolean needsQuoting = isReservedWord(tableName);
		return Identifier.toIdentifier(tableName, needsQuoting);
	}

	private boolean isReservedWord(String name) {
		// Common SQL reserved words that need quoting
		return "user".equalsIgnoreCase(name)
			|| "order".equalsIgnoreCase(name)
			|| "group".equalsIgnoreCase(name)
			|| "limit".equalsIgnoreCase(name)
			|| "select".equalsIgnoreCase(name)
			|| "table".equalsIgnoreCase(name)
			|| "index".equalsIgnoreCase(name);
	}
}
