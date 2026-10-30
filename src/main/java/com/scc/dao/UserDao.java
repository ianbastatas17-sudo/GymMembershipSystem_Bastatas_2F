
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    // Register a new user. New accounts start with PENDING status.
    public boolean register(
            String username,
            String password,
            String roles
    ) throws SQLException {

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, roles.toUpperCase(Locale.ROOT));

            return statement.executeUpdate() == 1;
        }
    }

    // Find a user by username.
    public User findByUsername(String username) throws SQLException {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        }

        return null;
    }

    // Get all users.
    public List<User> getAllUsers() throws SQLException {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    // Get users whose account status is PENDING.
    public List<User> getPendingUsers() throws SQLException {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE UPPER(status) = 'PENDING'
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    // Approve a pending account.
    public boolean approveUser(int userId) throws SQLException {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ? AND UPPER(status) = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() == 1;
        }
    }

    // Update a user's role and status using PreparedStatement.
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus
    ) throws SQLException {

        String role = newRole.trim().toUpperCase(Locale.ROOT);
        String status = newStatus.trim().toUpperCase(Locale.ROOT);

        // Validate inputs even if this method is called outside AdminMenu.
        if (!role.equals("STUDENT")
                && !role.equals("TEACHER")
                && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid user role.");
        }

        if (!status.equals("APPROVED")
                && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid account status.");
        }

        String sql =
                "UPDATE users SET roles = ?, status = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setString(2, status);
            statement.setInt(3, userId);

            return statement.executeUpdate() == 1;
        }
    }

    // Record a user's activity.
    public void logActivity(int userId, String action) throws SQLException {

        String sql = """
                INSERT INTO activity_logs (user_id, action)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, action);
            statement.executeUpdate();
        }
    }

    // Convert a database row into a User record.
    private User mapUser(ResultSet resultSet) throws SQLException {

        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("roles"),
                resultSet.getString("status")
        );
    }
}
