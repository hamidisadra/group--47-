package io.github.some_example_name;

import com.badlogic.gdx.Game;

import io.github.some_example_name.screens.MainScreen;
import io.github.some_example_name.screens.RegisterScreen;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.User;

public class Main extends Game {
    @Override
    public void create() {
        User loggedIn = UserManager.getInstance().findLoggedInUser();

        if (loggedIn != null) {
            MenuManager.getInstance().loginUser(loggedIn);
            setScreen(new MainScreen(this));
        }

        else {
            setScreen(new RegisterScreen(this));
        }
    }
}
