
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Menu {

    private final Scanner scanner;
    private final UserDao userDao;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
        this.userDao = new UserDao();
    }

    public void showMainMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n==============================");
            System.out.println("    GYM MEMBERSHIP SYSTEM");
            System.out.println("==============================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> registerAccount();
                case "2" -> loginAccount();
                case "3" -> {
                    running = false;
                    System.out.println("Application closed.");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void registerAccount() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.println("\nSelect Role:");
        System.out.println("1. TEACHER");
        System.out.println("2. STUDENT");
        System.out.print("Choose role: ");

        String roleChoice = scanner.nextLine().trim();

        String role;

        switch (roleChoice) {
            case "1" -> role = "TEACHER";
            case "2" -> role = "STUDENT";
            default -> {
                System.out.println("Invalid role selection.");
                return;
            }
        }

        if (username.isBlank() || password.isBlank()) {
            System.out.println(
                    "Username and password cannot be empty."
            );
            return;
        }

        boolean success =
                userDao.register(username, password, role);

        if (success) {
            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");
        }
    }

    private void loginAccount() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username());
        System.out.println("Role: " + user.roles());
        System.out.println("Status: " + user.status());

        if ("PENDING".equals(user.status())) {
            System.out.println(
                    "Your account is pending approval."
            );
        }

        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n--- ACCOUNT MENU ---");
            System.out.println("1. View Account");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("ID: " + user.id());
                    System.out.println(
                            "Username: " + user.username()
                    );
                    System.out.println("Role: " + user.roles());
                    System.out.println(
                            "Status: " + user.status()
                    );
                }
                case "2" -> {
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
