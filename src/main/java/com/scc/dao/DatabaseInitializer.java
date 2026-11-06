
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {

    public void initialize() {

        String[] sqlStatements = {

                // USERS TABLE
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (roles IN ('ADMIN', 'STAFF', 'MEMBER')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """,

                // MEMBERS TABLE
                """
                CREATE TABLE IF NOT EXISTS members (
                    member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER UNIQUE,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    contact_number TEXT,
                    address TEXT,
                    birthdate TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """,

                // MEMBERSHIP PLANS TABLE
                """
                CREATE TABLE IF NOT EXISTS membership_plans (
                    plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plan_name TEXT NOT NULL UNIQUE,
                    duration_months INTEGER NOT NULL,
                    price REAL NOT NULL CHECK (price >= 0)
                )
                """,

                // PAYMENTS TABLE
                """
                CREATE TABLE IF NOT EXISTS payments (
                    payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    plan_id INTEGER NOT NULL,
                    amount REAL NOT NULL CHECK (amount >= 0),
                    payment_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    payment_status TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY (member_id)
                        REFERENCES members(member_id),
                    FOREIGN KEY (plan_id)
                        REFERENCES membership_plans(plan_id)
                )
                """
        };

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement =
                     connection.createStatement()) {

            // Create existing system tables
            for (String sql : sqlStatements) {
                statement.executeUpdate(sql);
            }

            // Create or migrate activity_logs table
            initializeActivityLogs(connection);

            System.out.println(
                    "Database initialized successfully."
            );

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Database initialization failed.", e
            );
        }
    }

    private void initializeActivityLogs(Connection connection)
            throws SQLException {

        if (!tableExists(connection, "activity_logs")) {

            createActivityLogsTable(connection);

            return;
        }

        // Check if the table already has the new structure
        boolean hasId = columnExists(
                connection, "activity_logs", "id"
        );

        boolean hasActionType = columnExists(
                connection, "activity_logs", "action_type"
        );

        boolean hasDescription = columnExists(
                connection, "activity_logs", "description"
        );

        boolean hasTimestamp = columnExists(
                connection, "activity_logs", "timestamp"
        );

        if (hasId && hasActionType
                && hasDescription && hasTimestamp) {

            return;
        }

        // Preserve old activity log records during migration
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "ALTER TABLE activity_logs " +
                            "RENAME TO activity_logs_legacy"
            );
        }

        createActivityLogsTable(connection);

        // Copy records from the previous Deliverable 8 schema
        if (columnExists(
                connection, "activity_logs_legacy", "log_id")
                && columnExists(
                connection, "activity_logs_legacy", "action")
                && columnExists(
                connection, "activity_logs_legacy", "log_date")) {

            String migrationSQL = """
                    INSERT INTO activity_logs
                    (id, user_id, action_type, description, timestamp)
                    SELECT
                        log_id,
                        user_id,
                        COALESCE(action, 'LEGACY_ACTION'),
                        COALESCE(action, 'Migrated old activity log'),
                        COALESCE(log_date, CURRENT_TIMESTAMP)
                    FROM activity_logs_legacy
                    """;

            try (Statement statement =
                         connection.createStatement()) {

                statement.executeUpdate(migrationSQL);
            }
        }

        // Remove old table after successful migration
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "DROP TABLE activity_logs_legacy"
            );
        }

        System.out.println(
                "Activity logs table migrated successfully."
        );
    }

    private void createActivityLogsTable(Connection connection)
            throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                )
                """;

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private boolean tableExists(
            Connection connection, String tableName)
            throws SQLException {

        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table' AND name = ?
                """;

        try (var statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, tableName);

            try (ResultSet result = statement.executeQuery()) {

                return result.next();
            }
        }
    }

    private boolean columnExists(
            Connection connection,
            String tableName,
            String columnName) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {

                if (columnName.equalsIgnoreCase(
                        result.getString("name"))) {

                    return true;
                }
            }
        }

        return false;
    }
}
