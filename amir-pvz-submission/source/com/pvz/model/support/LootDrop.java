package com.pvz.model.support;

import com.pvz.model.enums.LootType;

public final class LootDrop {
    public final LootType type;
    public final GridPosition position;
    public LootDrop(LootType type, GridPosition position) {
        this.type = type;
        this.position = position;
    }
}
