
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    // Register a new user
    public boolean register(
            String username,
            String password,
            String roles
    ) throws SQLException {

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3,
                    roles.toUpperCase(Locale.ROOT));

            return statement.executeUpdate() == 1;
        }
    }

    // Find user by username
    public User findByUsername(String username)
            throws SQLException {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        }

        return null;
    }

    // Get all users
    public List<User> getAllUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    // Get pending users
    public List<User> getPendingUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE UPPER(status) = 'PENDING'
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }

        return users;
    }

    // Approve user (compatible with old AdminMenu call)
    public boolean approveUser(int userId) throws SQLException {
        return approveUser(userId, null);
    }

    // Approve user and record the admin who performed the action
    public boolean approveUser(int userId, Integer adminId)
            throws SQLException {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ? AND UPPER(status) = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int updatedRows;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setInt(1, userId);
                    updatedRows = statement.executeUpdate();
                }

                if (updatedRows == 1) {

                    insertLog(
                            connection,
                            adminId,
                            "USER_APPROVED",
                            "Admin [" + adminId +
                                    "] approved account for User ID " + userId
                    );
                }

                connection.commit();

                return updatedRows == 1;

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }

    // Update role and status (compatible with old AdminMenu call)
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus
    ) throws SQLException {

        return updateUserRoleAndStatus(
                userId, newRole, newStatus, null
        );
    }

    // Update role and status and record the admin action
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus,
            Integer adminId
    ) throws SQLException {

        String role = newRole.trim().toUpperCase(Locale.ROOT);
        String status = newStatus.trim().toUpperCase(Locale.ROOT);

        // Validate roles based on DatabaseInitializer
        if (!role.equals("ADMIN")
                && !role.equals("STAFF")
                && !role.equals("MEMBER")) {

            throw new IllegalArgumentException(
                    "Invalid role. Use ADMIN, STAFF, or MEMBER."
            );
        }

        if (!status.equals("APPROVED")
                && !status.equals("PENDING")
                && !status.equals("ACTIVE")) {

            throw new IllegalArgumentException(
                    "Invalid account status."
            );
        }

        String sql = """
                UPDATE users
                SET roles = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int updatedRows;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, role);
                    statement.setString(2, status);
                    statement.setInt(3, userId);

                    updatedRows = statement.executeUpdate();
                }

                if (updatedRows == 1) {

                    insertLog(
                            connection,
                            adminId,
                            "USER_UPDATED",
                            "Admin [" + adminId +
                                    "] changed User " + userId +
                                    " role to " + role +
                                    " and status to " + status
                    );
                }

                connection.commit();

                return updatedRows == 1;

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }

    // Insert audit log using the same database transaction
    private void insertLog(
            Connection connection,
            Integer userId,
            String actionType,
            String description
    ) throws SQLException {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (userId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, userId);
            }

            statement.setString(2, actionType);
            statement.setString(3, description);

            statement.executeUpdate();
        }
    }

    // Keep compatibility with existing logActivity calls
    public void logActivity(int userId, String action)
            throws SQLException {

        new ActivityLogDao().logAction(
                userId,
                "ACTIVITY",
                action
        );
    }

    // Convert database row into User record
    private User mapUser(ResultSet resultSet)
            throws SQLException {

        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("roles"),
                resultSet.getString("status")
        );
    }
}
