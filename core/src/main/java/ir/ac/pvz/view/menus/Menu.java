package ir.ac.pvz.view.menus;

import ir.ac.pvz.controller.managers.MenuManager;

public abstract class Menu {
    protected MenuManager menuManager;
    protected String name;

    public Menu(String name) {
        menuManager = MenuManager.getInstance();
        this.name = name;
    }

    public void showMenu() {
        System.out.println(name);
    }

    public abstract void executeCommand(String command);
}
