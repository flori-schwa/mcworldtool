package me.shawlaf.mcworldtool.region;

import dev.dewy.nbt.io.CompressionType;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionChunk {

    @Getter
    private final int compressedSize;
    @Getter
    private final CompressionType compressionType;
    @Getter
    private byte[] compressedChunk;

    @SneakyThrows
    public static RegionChunk parse(DataInputStream in) throws EOFException {
        int size = in.readInt();
        int compressionScheme = in.readUnsignedByte();

        RegionChunk chunk = switch (compressionScheme) {
            case 0 -> new RegionChunk(size, CompressionType.NONE);
            case 1 -> new RegionChunk(size, CompressionType.GZIP);
            case 2 -> new RegionChunk(size, CompressionType.ZLIB);
            default -> throw new RuntimeException("Unknown compression scheme: %d".formatted(compressionScheme));
        };

        chunk.compressedChunk = new byte[chunk.getCompressedSize()];
        in.readFully(chunk.compressedChunk, 0, chunk.getCompressedSize());

        return chunk;
    }

    @SneakyThrows
    public DataInput streamChunkNbt() {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.compressedChunk);

        return new DataInputStream(switch (this.compressionType) {
            case NONE -> bais;
            case ZLIB -> new InflaterInputStream(bais);
            case GZIP -> new GZIPInputStream(bais);
        });
    }

    public int calculateSectorCount() {
        int sizeBytes = 5 + compressedChunk.length;
        int sectors = sizeBytes / RegionUtil.FOUR_KB;

        if (sizeBytes % RegionUtil.FOUR_KB != 0) {
            ++sectors;
        }

        return sectors;
    }

    private RegionChunk(int compressedSize, CompressionType compressionType) {
        this.compressedSize = compressedSize;
        this.compressionType = compressionType;
    }

    /**
     * @return Amount of sectors written
     */
    @SneakyThrows
    public int write(DataOutput out) {
        int written = 0;

        out.writeInt(compressedSize);
        written += 4;

        out.writeByte(this.compressionType.ordinal());
        written += 1;

        out.write(this.compressedChunk, 0, this.compressedSize);
        written += compressedSize;

        int remainder = written % RegionUtil.FOUR_KB;

        if (remainder != 0) {
            int padding = RegionUtil.FOUR_KB - remainder;
            byte[] paddingBytes = new byte[padding]; // Automatically initialized with all zeros

            out.write(paddingBytes, 0, padding);
            written += padding;
        }

        return written / RegionUtil.FOUR_KB;
    }
}
