package ir.ac.pvz.controller.managers;

import ir.ac.pvz.view.Menu;
import ir.ac.pvz.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MenuManager {
    private static MenuManager instance;
    private Stack<Menu> menuStack;
    private User activeUser;

    private MenuManager() {
        menuStack = new Stack<>();
    }

    public static MenuManager getInstance() {
        if (instance == null) instance = new MenuManager();

        return instance;
    }

    public void pushMenu(Menu menu) {
        menuStack.push(menu);
    }

    public void popMenu() {
        if (!menuStack.isEmpty()) {
            menuStack.pop();
        }
    }

    public Menu getActiveMenu() {
        if (menuStack.empty()) {
            return null;
        }
        return menuStack.peek();
    }

    public void loginUser(User user) {
        this.activeUser = user;
    }

    public void logOutUser() {
        this.activeUser = null;
    }

    public void cheat(int amount, String type) {

    }

    /*
    public List<LeaderBoardEntry> getLeaderBoardEntry() {
        return new ArrayList<>();
    }
     */

    public User getActiveUser() {
        return activeUser;
    }
}
