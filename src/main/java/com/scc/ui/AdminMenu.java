
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class    AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
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
            System.out.println("0. Logout");
            System.out.println("==============================================");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> viewAllUsers();
                    case "2" -> viewPendingUsers();
                    case "3" -> approveUserAccount();
                    case "0" -> {
                        System.out.println("Admin logged out.");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println(
                        "Database error: " + e.getMessage()
                );
            }
        }
    }

    private void viewAllUsers() throws SQLException {
        List<User> users = userDao.getAllUsers();

        System.out.println("\nALL SYSTEM USERS");
        printUsersTable(users);
    }

    private void viewPendingUsers() throws SQLException {
        List<User> users = userDao.getPendingUsers();

        System.out.println("\nPENDING REGISTRATIONS");
        printUsersTable(users);
    }

    private void approveUserAccount() throws SQLException {
        List<User> pendingUsers = userDao.getPendingUsers();

        System.out.println("\nPENDING REGISTRATIONS");
        printUsersTable(pendingUsers);

        if (pendingUsers.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }

        System.out.print(
                "Enter User ID to approve (0 to cancel): "
        );

        String input = scanner.nextLine().trim();

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Enter a numeric ID.");
            return;
        }

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean found = pendingUsers.stream()
                .anyMatch(user -> user.id() == userId);

        if (!found) {
            System.out.println(
                    "User ID not found in the pending list."
            );
            return;
        }

        boolean success = userDao.approveUser(userId);

        if (success) {
            System.out.println(
                    "SUCCESS: User ID " + userId
                            + " has been approved."
            );
        } else {
            System.out.println(
                    "Approval failed. The account may already be updated."
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
            System.out.printf(
                    "| %-53s |%n", "No users found."
            );
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
