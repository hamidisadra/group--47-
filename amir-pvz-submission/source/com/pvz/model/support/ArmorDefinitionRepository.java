package com.pvz.model.support;

public interface ArmorDefinitionRepository {
    int getHealth(String alias);
    boolean isMetallic(String alias);
}
