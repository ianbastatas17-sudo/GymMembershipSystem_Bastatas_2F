
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== GYM MEMBERSHIP SYSTEM ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (username.isBlank() || password.isBlank()) {
                System.out.println("Fields cannot be empty.");
                return;
            }

            System.out.println("1. MEMBER");
            System.out.println("2. STAFF");
            System.out.print("Select role: ");

            String roleChoice = scanner.nextLine().trim();

            String role = switch (roleChoice) {
                case "1" -> "MEMBER";
                case "2" -> "STAFF";
                default -> null;
            };

            if (role == null) {
                System.out.println("Invalid role.");
                return;
            }

            if (userDao.register(username, password, role)) {
                System.out.println("Registration successful!");
                System.out.println("Account status: PENDING");
            } else {
                System.out.println("Username already exists.");
            }

        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            User user = userDao.findByUsername(username);

            if (user == null || !user.password().equals(password)) {
                System.out.println("Invalid username or password.");
                return;
            }

            boolean isAdmin =
                    "ADMIN".equalsIgnoreCase(user.roles());

            boolean isApproved =
                    "APPROVED".equalsIgnoreCase(user.status());

            boolean isExistingActiveAdmin =
                    isAdmin && "ACTIVE".equalsIgnoreCase(user.status());

            if (!isApproved && !isExistingActiveAdmin) {
                System.out.println(
                        "Account is still " + user.status()
                );
                return;
            }

            System.out.println("Welcome, " + user.username() + "!");

            switch (user.roles().toUpperCase()) {
                case "ADMIN" -> new AdminMenu(userDao, scanner).display();
                case "STAFF" -> showRoleMenu("STAFF");
                case "MEMBER" -> showRoleMenu("MEMBER");
                default -> System.out.println("Unknown role.");
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    private void showRoleMenu(String role) {
        while (true) {
            System.out.println("\n=== " + role + " DASHBOARD ===");
            System.out.println("1. Logout");
            System.out.print("Choose: ");

            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("Logged out.");
                return;
            }

            System.out.println("Invalid choice.");
        }
    }
}
