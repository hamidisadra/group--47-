package com.pvz.model.support;

import java.util.HashMap;
import java.util.Map;

public class SeedPacketInventory {

    public Map<String, Integer> packetsByPlant;

    public SeedPacketInventory() {
        packetsByPlant = new HashMap<>();
    }

    public int getPackets(String plantType) {
        return packetsByPlant.getOrDefault(plantType, 0);
    }

    public boolean spendPackets(String plantType, int count) {
        int current = getPackets(plantType);
        if (current < count) {
            return false;
        }
        packetsByPlant.put(plantType, current - count);
        return true;
    }

    public void addPackets(String plantType, int count) {
        packetsByPlant.put(plantType, getPackets(plantType) + count);
    }
}
