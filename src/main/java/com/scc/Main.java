
package com.scc;

import com.scc.dao.DatabaseConnection;
import com.scc.ui.Menu;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        DatabaseConnection.initializeDatabase();

        try (Scanner scanner = new Scanner(System.in)) {
            Menu menu = new Menu(scanner);
            menu.showMainMenu();
        }
    }
}
