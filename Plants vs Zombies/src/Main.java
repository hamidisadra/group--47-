import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.menus.MainMenu;
import ir.ac.pvz.view.menus.RegisterMenu;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        MenuManager menuManager = MenuManager.getInstance();
        UserManager userManager = UserManager.getInstance();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
        menuManager.setInputOutput(in, out);

        User previousUser = userManager.findLoggedInUser();

        if (previousUser == null) {
            menuManager.pushMenu(new RegisterMenu());
        }
        else {
            System.out.println("Welcome back!");

            menuManager.loginUser(previousUser);
            menuManager.pushMenu(new MainMenu());
        }

        String command;
        while (menuManager.getActiveMenu() != null && (command = in.readLine()) != null) {
            menuManager.getActiveMenu().executeCommand(command);
        }
    }
}
