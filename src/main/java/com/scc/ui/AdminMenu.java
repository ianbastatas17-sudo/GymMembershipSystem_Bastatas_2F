
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.User;
import com.scc.model.ActivityLog;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner, User currentAdmin) {
        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void display() {
        boolean running = true;

        while (running) {
            System.out.println("\n==============================================");
            System.out.println("                 ADMIN MENU");
            System.out.println("==============================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("0. Logout");
            System.out.println("==============================================");

            int choice = readIntInput("Enter choice (0 to logout): ");

            try {
                switch (choice) {
                    case 1 -> viewAllUsers();
                    case 2 -> viewPendingUsers();
                    case 3 -> approveUserAccount();
                    case 4 -> updateUser();
                    case 5 -> viewAuditLogs();
                    case 0 -> {
                        logAdminLogout();
                        System.out.println("Admin logged out.");
                        running = false;
                    }
                    default -> System.out.println("Invalid menu choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void viewAllUsers() throws SQLException {
        System.out.println("\nALL SYSTEM USERS");
        printUsersTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() throws SQLException {
        System.out.println("\nPENDING REGISTRATIONS");
        printUsersTable(userDao.getPendingUsers());
    }

    private void approveUserAccount() throws SQLException {
        List<User> pendingUsers = userDao.getPendingUsers();

        System.out.println("\nPENDING REGISTRATIONS");
        printUsersTable(pendingUsers);

        if (pendingUsers.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }

        int userId = readIntInput(
                "Enter User ID to approve (0 to cancel): "
        );

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean found = pendingUsers.stream()
                .anyMatch(user -> user.id() == userId);

        if (!found) {
            System.out.println("User ID not found in pending list.");
            return;
        }

        if (userDao.approveUser(userId, currentAdmin.id())) {
            System.out.println(
                    "SUCCESS: User ID " + userId + " has been approved."
            );
        } else {
            System.out.println("Approval failed.");
        }
    }

    private void updateUser() throws SQLException {
        List<User> users = userDao.getAllUsers();

        System.out.println("\nUPDATE USER ROLE AND STATUS");
        printUsersTable(users);

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        int targetUserId = readIntInput(
                "Enter target User ID (0 to cancel): "
        );

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User targetUser = users.stream()
                .filter(user -> user.id() == targetUserId)
                .findFirst()
                .orElse(null);

        if (targetUser == null) {
            System.out.println("User ID not found.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted."
            );
            return;
        }

        System.out.println("Selected user: " + targetUser.username());
        System.out.println("Enter 0 at any prompt to cancel.");

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus,
                currentAdmin.id()
        );

        if (success) {
            System.out.println(
                    "SUCCESS: User role and status updated successfully."
            );
        } else {
            System.out.println("Update failed. No user record was changed.");
        }
    }

    private void viewAuditLogs() throws SQLException {
        System.out.println("\n==============================================");
        System.out.println("             SYSTEM AUDIT LOGS");
        System.out.println("==============================================");
        System.out.println("1. View All System Logs");
        System.out.println("2. Filter Logs by User ID");
        System.out.println("0. Back to Admin Menu");

        int choice = readIntInput("Enter choice: ");

        switch (choice) {
            case 1 -> {
                System.out.println("\nALL SYSTEM AUDIT LOGS");
                printLogsTable(activityLogDao.getAllLogs());
            }
            case 2 -> {
                int userId = readIntInput(
                        "Enter User ID to filter (0 to cancel): "
                );

                if (userId == 0) {
                    System.out.println("Filter cancelled.");
                    return;
                }

                System.out.println("\nAUDIT LOGS FOR USER ID: " + userId);
                printLogsTable(activityLogDao.getLogsByUserId(userId));
            }
            case 0 -> System.out.println("Returning to Admin Menu...");
            default -> System.out.println("Invalid choice.");
        }
    }

    private void printLogsTable(List<ActivityLog> logs) {
        String border = "-----------------------------------------------------------------------------------------------";

        System.out.println(border);

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(border);

        if (logs.isEmpty()) {
            System.out.println("No audit logs found.");
        } else {
            for (ActivityLog log : logs) {
                System.out.printf(
                        "%-5d | %-8s | %-18s | %-35s | %-20s%n",
                        log.id(),
                        log.userId(),
                        shorten(log.actionType(), 18),
                        shorten(log.description(), 35),
                        shorten(log.timestamp(), 20)
                );
            }
        }

        System.out.println(border);
    }

    private void logAdminLogout() {
        try {
            activityLogDao.logAction(
                    currentAdmin.id(),
                    "LOGOUT",
                    "Admin " + currentAdmin.username() + " logged out."
            );
        } catch (SQLException e) {
            System.out.println(
                    "Warning: Could not save logout activity log: "
                            + e.getMessage()
            );
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Enter a number or 0 to cancel."
                );
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print(
                    "Enter role (MEMBER, STAFF, ADMIN; 0 to cancel): "
            );

            String role = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (role.equals("0")) {
                return null;
            }

            if (role.equals("MEMBER")
                    || role.equals("STAFF")
                    || role.equals("ADMIN")) {
                return role;
            }

            System.out.println(
                    "Invalid role. Choose MEMBER, STAFF, or ADMIN."
            );
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print(
                    "Enter status (APPROVED, PENDING, ACTIVE; 0 to cancel): "
            );

            String status = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (status.equals("0")) {
                return null;
            }

            if (status.equals("APPROVED")
                    || status.equals("PENDING")
                    || status.equals("ACTIVE")) {
                return status;
            }

            System.out.println(
                    "Invalid status. Choose APPROVED, PENDING, or ACTIVE."
            );
        }
    }

    private void printUsersTable(List<User> users) {
        String border =
                "+-------+-----------------+--------------+------------+";

        System.out.println(border);
        System.out.printf(
                "| %-5s | %-15s | %-12s | %-10s |%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-53s |%n", "No users found.");
        } else {
            for (User user : users) {
                System.out.printf(
                        "| %-5d | %-15s | %-12s | %-10s |%n",
                        user.id(),
                        shorten(user.username(), 15),
                        shorten(user.roles(), 12),
                        shorten(user.status(), 10)
                );
            }
        }

        System.out.println(border);
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}
