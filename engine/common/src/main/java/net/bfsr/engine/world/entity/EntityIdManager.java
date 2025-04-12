package net.bfsr.engine.world.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class EntityIdManager {
    protected int nextId;
    protected final IntList freeIdList = new IntArrayList();

    public int getNextId() {
        return nextId++;
    }

    public void returnBackId(int id) {
        freeIdList.add(id);
    }
}