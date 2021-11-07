package me.shawlaf.mcworldtool.anvil;

import dev.dewy.nbt.tags.collection.CompoundTag;
import lombok.Getter;
import lombok.SneakyThrows;
import me.shawlaf.mcworldtool.anvil.versions.AnvilChunkGeneral;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class AnvilChunk {

    private static final Map<Integer, Class<? extends AnvilChunk>> versionImplementations = new HashMap<>();

    static {
        // All known versions store InhabitedTime in Level Tag -> No need to add specific Version implementations
    }

    @SneakyThrows
    public static AnvilChunk withTag(CompoundTag tag) {
        int dataVersion = tag.getInt("DataVersion").getValue();
        Class<? extends AnvilChunk> clazz = versionImplementations.getOrDefault(dataVersion, AnvilChunkGeneral.class);
        Constructor<? extends AnvilChunk> constructor = clazz.getConstructor(CompoundTag.class);

        return constructor.newInstance(tag);
    }

    @Getter
    private final CompoundTag tag;

    public AnvilChunk(CompoundTag tag) {
        this.tag = tag;
    }

    public int getDataVersion() {
        return tag.getInt("DataVersion").getValue();
    }

    public abstract long getInhabitedTime();
}
