package me.shawlaf.mcworldtool.anvil.versions;

import dev.dewy.nbt.tags.collection.CompoundTag;
import me.shawlaf.mcworldtool.anvil.AnvilChunk;

public class AnvilChunkGeneral extends AnvilChunk {
    public AnvilChunkGeneral(CompoundTag tag) {
        super(tag);
    }

    @Override
    public long getInhabitedTime() {
        CompoundTag level = getTag().getCompound("Level");

        return level.getLong("InhabitedTime").getValue();
    }
}
