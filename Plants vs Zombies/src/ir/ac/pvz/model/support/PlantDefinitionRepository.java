package ir.ac.pvz.model.support;

import ir.ac.pvz.model.core.Plant;

import java.util.List;

public interface PlantDefinitionRepository {

    PlantDefinition get(String plantType);

    List<PlantDefinition> getAll();

    void applyTo(Plant plant);
}
