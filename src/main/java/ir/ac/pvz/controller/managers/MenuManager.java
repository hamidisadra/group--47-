package ir.ac.pvz.controller.managers;

import ir.ac.pvz.model.user.User;

import java.util.ArrayDeque;
import java.util.Deque;

public class MenuManager {
    private static MenuManager instance;

    private final Deque<ScreenId> history;
    private ScreenId currentScreen;
    private User activeUser;
    private NavigationListener navigationListener;

    private MenuManager() {
        history = new ArrayDeque<>();
        currentScreen = ScreenId.LOGIN;
    }

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    public void goTo(ScreenId screen) {
        if (screen == null || screen == currentScreen) {
            return;
        }
        history.push(currentScreen);
        currentScreen = screen;
        fireNavigation();
    }

    public void replaceWith(ScreenId screen) {
        if (screen == null) {
            return;
        }
        history.clear();
        currentScreen = screen;
        fireNavigation();
    }

    public boolean goBack() {
        if (history.isEmpty()) {
            return false;
        }
        currentScreen = history.pop();
        fireNavigation();
        return true;
    }

    public ScreenId getCurrentScreen() {
        return currentScreen;
    }

    public void loginUser(User user) {
        this.activeUser = user;
    }

    public void logoutUser() {
        this.activeUser = null;
        history.clear();
        currentScreen = ScreenId.LOGIN;
        fireNavigation();
    }

    public User getActiveUser() {
        return activeUser;
    }

    private void fireNavigation() {
        if (navigationListener != null) {
            navigationListener.onScreenChanged(currentScreen);
        }
    }

    public interface NavigationListener {
        void onScreenChanged(ScreenId screen);
    }
}
