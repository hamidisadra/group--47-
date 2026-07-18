package ir.ac.pvz.view.menus;

import ir.ac.pvz.model.travel.TravelLog;
import ir.ac.pvz.model.user.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TravelLogMenu extends Menu {
    private TravelLog travelLog;

    public TravelLogMenu() {
        super("Travel Log Menu");
        this.travelLog = new TravelLog();
    }

    @Override
    public void executeCommand(String command) {
        command = command.trim();

        String pageRegex = "^travel\\s+log\\s+page\\s+(\\S+)$";
        Matcher pageMatcher = Pattern.compile(pageRegex).matcher(command);

        if (pageMatcher.matches()) {
            String pageName = pageMatcher.group(1).toLowerCase();
            changePage(pageName);
        }

        else if (command.matches("^menu\\s+show\\s+current$")) {
            showMenu();
        }

        else if (command.matches("^menu\\s+exit$")) {
            menuManager.popMenu();
        }

        else {
            System.out.println("Invalid command.");
        }
    }

    private void changePage(String pageName) {
        travelLog.changePage(pageName);

        switch (pageName) {
            case "vasebreaker":
                menuManager.pushMenu(new VasebreakerMenu());
                break;
            case "wallnut-bowling":
                menuManager.pushMenu(new WallnutBowlingMenu());
                break;
            case "izombie":
                menuManager.pushMenu(new IZombieMenu());
                break;
            case "beghouled":
                menuManager.pushMenu(new BeghouledMenu());
                break;
            case "zombotany":
                menuManager.pushMenu(new ZombotanyMenu());
                break;
            default: {
                User user = menuManager.getActiveUser();
                if (user == null) {
                    System.out.println("Error: No active user found!");
                    return;
                }
                travelLog.showPage(user.getQuestLog());
                break;
            }
        }
    }
}
