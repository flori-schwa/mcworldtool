package me.shawlaf.mcworldtool.region;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.DataInput;
import java.io.DataOutput;

public class RegionChunkLocation {

    public static final RegionChunkLocation EMPTY = new RegionChunkLocation(0, 0);

    @Getter
    private final int offset;
    @Getter
    private final int sectorCount;

    @SneakyThrows
    public static RegionChunkLocation read(DataInput in) {
        int location = in.readInt();

        return new RegionChunkLocation(location >> 8, location & 0xFF);
    }

    public RegionChunkLocation(int offset, int sectorCount) {
        this.offset = offset;
        this.sectorCount = sectorCount;
    }

    public boolean isEmpty() {
        return offset == 0 && sectorCount == 0;
    }

    @SneakyThrows
    public void write(DataOutput out) {
        out.writeInt((this.offset << 8) | (this.sectorCount & 0xFF));
    }
}
