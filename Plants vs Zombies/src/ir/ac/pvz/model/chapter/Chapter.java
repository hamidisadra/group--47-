package ir.ac.pvz.model.chapter;

import ir.ac.pvz.model.stage.Stage;
import ir.ac.pvz.model.support.Board;

import java.util.ArrayList;
import java.util.List;

public abstract class Chapter {
    protected String name;
    protected List<Stage> stages;

    public Chapter(String name) {
        this.name = name;
        this.stages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public Stage getStage(int index) {
        if (index < 0 || index >= stages.size()) {
            return null;
        }
        return stages.get(index);
    }

    public void startChapter() {
        System.out.println("Entering chapter: " + name);
    }

    public abstract void applyChapterEffects(Board board);
}