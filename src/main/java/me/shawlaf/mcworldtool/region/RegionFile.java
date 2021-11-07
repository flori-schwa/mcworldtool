package me.shawlaf.mcworldtool.region;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class RegionFile {

    private static final Nbt NBT = new Nbt();

    @Getter
    private final RegionFileHeader header;
    private final RegionChunk[] chunks = new RegionChunk[1024];

    public RegionFile() {
        this.header = RegionFileHeader.newHeader();
    }

    @SneakyThrows
    public RegionFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            try (DataInputStream in = new DataInputStream(fis)) {
                this.header = RegionFileHeader.parse(in);
            }
        }

        for (int cx = 0; cx < RegionUtil.REGION_WIDTH; cx++) {
            for (int cz = 0; cz < RegionUtil.REGION_WIDTH; cz++) {
                RegionChunkLocation loc = header.getChunkLocation(cx, cz);

                if (loc.isEmpty()) {
                    continue;
                }

                try (DataInputStream in = readChunkData(file, loc)) {
                    this.chunks[RegionUtil.chunkIndex(cx, cz)] = RegionChunk.parse(in);
                }
            }
        }
    }

    @SneakyThrows
    @NotNull
    private DataInputStream readChunkData(File file, RegionChunkLocation location) {
        if (location.isEmpty()) {
            throw new RuntimeException("Cannot read data of empty Chunk Location");
        }

        FileInputStream fis = new FileInputStream(file);
        fis.skipNBytes((1024 * 4) * location.getOffset());

        return new DataInputStream(fis);
    }

    @Nullable
    public RegionChunk getChunkDataAt(int chunkX, int chunkZ) {
        return chunks[RegionUtil.chunkIndex(chunkX, chunkZ)];
    }

    public void addChunk(int chunkX, int chunkZ, RegionChunk chunk) {
        this.chunks[RegionUtil.chunkIndex(chunkX, chunkZ)] = chunk;
    }

    @SneakyThrows
    public CompoundTag loadChunk(RegionChunk chunk) {
        return NBT.fromStream(chunk.streamChunkNbt());
    }

    @SneakyThrows
    @Nullable
    public CompoundTag loadChunkAt(int chunkX, int chunkZ) {
        RegionChunk data = getChunkDataAt(chunkX, chunkZ);

        if (data == null) {
            return null;
        }

        return loadChunk(data);
    }

    @SneakyThrows
    public void write(OutputStream out) {
        RegionFileHeader writeHeader = RegionFileHeader.newHeader();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        int offset = 2; // Directly after header

        try (DataOutputStream bodyOut = new DataOutputStream(body)) {
            for (int i = 0; i < chunks.length; i++) {
                RegionChunk chunk = this.chunks[i];

                if (chunk == null) {
                    continue;
                }

                int chunkOffset = offset;
                int chunkSectors = chunk.write(bodyOut);

                offset += chunkSectors;

                writeHeader.setChunkLocation(i, new RegionChunkLocation(chunkOffset, chunkSectors));
            }
        }

        try (DataOutputStream dataOut = new DataOutputStream(out)) {
            writeHeader.write(dataOut);
            body.writeTo(out);
        }
    }
}
