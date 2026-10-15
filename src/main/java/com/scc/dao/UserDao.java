
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public boolean register(
            String username,
            String password,
            String roles
    ) {
        String sql = """
                INSERT INTO users (username, password, roles)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, roles);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed. Username may already exist."
            );
            return false;
        }
    }

    public User login(String username, String password) {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login failed: " + e.getMessage()
            );
        }

        return null;
    }
}
