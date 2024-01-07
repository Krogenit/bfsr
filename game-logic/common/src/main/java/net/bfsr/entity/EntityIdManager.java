package net.bfsr.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class EntityIdManager {
    protected int nextId;

    public int getNextId() {
        return nextId++;
    }
}