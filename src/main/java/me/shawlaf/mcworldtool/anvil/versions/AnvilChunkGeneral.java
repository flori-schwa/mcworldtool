package me.shawlaf.mcworldtool.anvil.versions;

import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import me.shawlaf.mcworldtool.anvil.AnvilChunk;

public class AnvilChunkGeneral extends AnvilChunk {
    public AnvilChunkGeneral(CompoundTag tag) {
        super(tag);
    }

    private CompoundTag getLevel() {
        return getTag().getCompound("Level");
    }

    @Override
    public long getInhabitedTime() {
        return getLevel().getLong("InhabitedTime").getValue();
    }

    @Override
    public ListTag<CompoundTag> getTileEntities() {
        return getLevel().getList("TileEntities");
    }
}
