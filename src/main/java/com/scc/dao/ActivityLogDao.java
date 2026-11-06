
package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Save a new activity log
    public void logAction(int userId, String actionType,
                          String description) throws SQLException {

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType.toUpperCase());
            statement.setString(3, description);

            statement.executeUpdate();
        }
    }

    // Get all activity logs
    public List<ActivityLog> getAllLogs() throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                ORDER BY timestamp DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                logs.add(new ActivityLog(
                        result.getInt("id"),
                        result.getInt("user_id"),
                        result.getString("action_type"),
                        result.getString("description"),
                        result.getString("timestamp")
                ));
            }
        }

        return logs;
    }

    // Get logs by specific user ID
    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    logs.add(new ActivityLog(
                            result.getInt("id"),
                            result.getInt("user_id"),
                            result.getString("action_type"),
                            result.getString("description"),
                            result.getString("timestamp")
                    ));
                }
            }
        }

        return logs;
    }
}
