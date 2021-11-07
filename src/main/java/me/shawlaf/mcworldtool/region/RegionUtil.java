package me.shawlaf.mcworldtool.region;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegionUtil {

    public static final int REGION_WIDTH = 32;

    public static final int CHUNKS_IN_REGION = REGION_WIDTH * REGION_WIDTH;

    public static final int ONE_KB = 1024;

    public static final int FOUR_KB = ONE_KB * 4;

    public int chunkIndex(int chunkX, int chunkZ) {
        return (chunkX & 31) + (chunkZ & 31) * 32;
    }
}
