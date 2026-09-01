package ir.ac.pvz.model.support;

import ir.ac.pvz.model.enums.ProjectileTrajectory;
import ir.ac.pvz.model.enums.ProjectileType;

public class ShotEvent {
    public final ProjectileType type;
    public final ProjectileTrajectory trajectory;
    public final float sourceColumn;
    public final int sourceRow;
    public final float targetColumn;
    public final int targetRow;
    public final ir.ac.pvz.model.core.Plant source;

    public ShotEvent(ProjectileType type, ProjectileTrajectory trajectory, float sourceColumn,
                     int sourceRow, float targetColumn, int targetRow,
                     ir.ac.pvz.model.core.Plant source) {
        this.type = type;
        this.trajectory = trajectory;
        this.sourceColumn = sourceColumn;
        this.sourceRow = sourceRow;
        this.targetColumn = targetColumn;
        this.targetRow = targetRow;
        this.source = source;
    }

    public interface Listener {
        void onShotFired(ShotEvent event);
    }
}
