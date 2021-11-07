package me.shawlaf.mcworldtool.region;

import lombok.SneakyThrows;

import java.io.DataInput;
import java.io.DataOutput;
import java.time.Instant;
import java.util.Optional;

public class RegionFileHeader {

    private final RegionChunkLocation[] locations = new RegionChunkLocation[RegionUtil.CHUNKS_IN_REGION];
    private final int[] timestamps = new int[RegionUtil.CHUNKS_IN_REGION];

    public static RegionFileHeader newHeader() {
        return new RegionFileHeader();
    }

    public static RegionFileHeader parse(DataInput in) {
        return new RegionFileHeader(in);
    }

    private RegionFileHeader() {

    }

    @SneakyThrows
    private RegionFileHeader(DataInput in) {
        for (int i = 0; i < locations.length; i++) {
            locations[i] = RegionChunkLocation.read(in);
        }

        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = in.readInt();
        }
    }

    public RegionChunkLocation getChunkLocation(int chunkX, int chunkZ) {
        return locations[RegionUtil.chunkIndex(chunkX, chunkZ)];
    }

    public void setChunkLocation(int chunkX, int chunkZ, RegionChunkLocation loc) {
        setChunkLocation(RegionUtil.chunkIndex(chunkX, chunkZ), loc);
    }

    public void setChunkLocation(int idx, RegionChunkLocation loc) {
        this.locations[idx] = loc;
        this.timestamps[idx] = (int) Instant.now().getEpochSecond();
    }

    public int getChunkModificationTimestamp(int chunkX, int chunkZ) {
        return timestamps[RegionUtil.chunkIndex(chunkX, chunkZ)];
    }

    @SneakyThrows
    public void write(DataOutput out) {
        for (int i = 0; i < locations.length; i++) {
            Optional.ofNullable(locations[i]).orElse(RegionChunkLocation.EMPTY).write(out);
        }

        for (int i = 0; i < timestamps.length; i++) {
            out.writeInt(timestamps[i]);
        }
    }

}
