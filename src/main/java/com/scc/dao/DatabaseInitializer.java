
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() {
        String[] sqlStatements = {
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

                """
            CREATE TABLE IF NOT EXISTS membership_plans (
                plan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                plan_name TEXT NOT NULL UNIQUE,
                duration_months INTEGER NOT NULL,
                price REAL NOT NULL CHECK (price >= 0)
            )
            """,

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
            """,

                """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                log_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """
        };

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement =
                     connection.createStatement()) {

            for (String sql : sqlStatements) {
                statement.executeUpdate(sql);
            }

            System.out.println(
                    "Database initialized successfully.");

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Database initialization failed.", e);
        }
    }
}
