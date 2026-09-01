package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.enums.SeasonType;
import ir.ac.pvz.view.ui.DialogueBox;

import java.util.ArrayList;
import java.util.List;

public final class DialogueScript {
    private DialogueScript() {
    }

    public static List<DialogueBox.Line> intro(SeasonType season, int stageNumber,
                                               boolean bossStage) {
        List<DialogueBox.Line> lines = new ArrayList<>();
        if (bossStage) {
            lines.add(new DialogueBox.Line("Crazy Dave", "CRAZYDAVE", "anim_crazyblahblah",
                    "WABBY WABBO! That is the big one, the boss of all zombies!"));
            lines.add(new DialogueBox.Line("Zomboss", "ZOMBOSS", "zomboss_talk",
                    "Your plants are no match for my machine. Prepare to lose your brains!"));
            lines.add(new DialogueBox.Line("Penny", "DAVEWINNIE_NARRATIONICONS", "winnie",
                    "User Dave, my sensors report three separate power cores. "
                            + "Break one and it will stop to recover."));
            return lines;
        }
        if (stageNumber != 1) {
            return lines;
        }
        switch (season) {
            case ANCIENT_EGYPT:
                lines.add(new DialogueBox.Line("Penny", "DAVEWINNIE_NARRATIONICONS", "winnie",
                        "We have arrived in Ancient Egypt, User Dave."));
                lines.add(new DialogueBox.Line("Crazy Dave", "CRAZYDAVE", "anim_mediumtalk",
                        "Watch out for the tombstones! And the tornadoes! And my taco!"));
                break;
            case FROSTBITE_CAVES:
                lines.add(new DialogueBox.Line("Penny", "DAVEWINNIE_NARRATIONICONS", "winnie",
                        "Temperature critical. Icy winds will freeze your plants."));
                lines.add(new DialogueBox.Line("Crazy Dave", "CRAZYDAVE", "anim_blahblah",
                        "BRRR! Plant something warm, quick!"));
                break;
            case BIG_WAVE_BEACH:
                lines.add(new DialogueBox.Line("Penny", "DAVEWINNIE_NARRATIONICONS", "winnie",
                        "The tide rises with every wave. Only water plants survive out there."));
                lines.add(new DialogueBox.Line("Crazy Dave", "CRAZYDAVE", "anim_mediumtalk",
                        "I love the beach! Except for the zombies. And the sand."));
                break;
            case DARK_AGES:
            default:
                lines.add(new DialogueBox.Line("Penny", "DAVEWINNIE_NARRATIONICONS", "winnie",
                        "The Dark Ages, User Dave. Graves everywhere, and necromancy afoot."));
                lines.add(new DialogueBox.Line("Crazy Dave", "CRAZYDAVE", "anim_crazyblahblah",
                        "A knight! A wizard! A jester! This is the best party ever!"));
                break;
        }
        return lines;
    }

    public static List<DialogueBox.Line> victory(boolean bossStage) {
        List<DialogueBox.Line> lines = new ArrayList<>();
        if (bossStage) {
            lines.add(new DialogueBox.Line("Zomboss", "ZOMBOSS", "zomboss_talk",
                    "Dear humanz, zis is not done yet. We will come back for your brainz!"));
            lines.add(new DialogueBox.Line("Crazy Dave", "CRAZYDAVE", "anim_crazyblahblah",
                    "We did it! Now where did I leave my car keys?"));
        }
        return lines;
    }
}
