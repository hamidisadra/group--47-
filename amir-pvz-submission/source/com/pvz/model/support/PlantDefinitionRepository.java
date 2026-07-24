package com.pvz.model.support;

import com.pvz.model.core.Plant;

import java.util.List;

public interface PlantDefinitionRepository {

    PlantDefinition get(String plantType);

    List<PlantDefinition> getAll();

    void applyTo(Plant plant);
}
