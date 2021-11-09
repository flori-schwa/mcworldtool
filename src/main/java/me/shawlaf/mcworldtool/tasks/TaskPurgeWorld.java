package me.shawlaf.mcworldtool.tasks;

import dev.dewy.nbt.tags.collection.CompoundTag;
import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import me.shawlaf.mcworldtool.IMcWorldToolOptions;
import me.shawlaf.mcworldtool.anvil.AnvilChunk;
import me.shawlaf.mcworldtool.region.RegionChunk;
import me.shawlaf.mcworldtool.region.RegionFile;
import me.shawlaf.mcworldtool.region.RegionUtil;
import me.shawlaf.mcworldtool.util.DirectoryTraversal;
import me.shawlaf.mcworldtool.util.FileUtil;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@ExtensionMethod({
        FileUtil.class
})
public class TaskPurgeWorld {

    private final IMcWorldToolOptions options;

    public static void run(IMcWorldToolOptions options) {
        new TaskPurgeWorld(options).run();
    }

    public TaskPurgeWorld(IMcWorldToolOptions options) {
        this.options = options;
    }

    @SneakyThrows
    public void run() {
        File inputFile = this.options.getInputPath();

        if (!inputFile.exists()) {
            System.err.println("Input Path does not exist");
            return;
        }

        if (!inputFile.isDirectory()) {
            System.err.println("Input Path is not a directory");
            return;
        }

        File outputPath = inputFile.getParentFile().getAbsoluteFile().child("%s-purged".formatted(inputFile.getName()));
        inputFile.copyDirectoryRecursively(outputPath);

        List<File> mcaFiles = DirectoryTraversal.discoverWhere(outputPath,
                f -> f.getName().endsWith(".mca") && "region".equals(f.getParentFile().getAbsoluteFile().getName())
        );

        int totalRemovedChunks;

        try (ProgressBar pb = new ProgressBarBuilder()
                .setTaskName("Purging Chunks")
                .setInitialMax(mcaFiles.size())
                .setStyle(ProgressBarStyle.ASCII)
                .setUpdateIntervalMillis(10)
                .build()
        ) {
            totalRemovedChunks = mcaFiles.parallelStream().mapToInt(file -> {
                try {
                    return purgeChunks(file);
                } catch (Exception e) {
                    // System.out.printf("Failed to purge Chunks from %s: %s%n", file.getName(), e.getMessage());
                    return 0;
                } finally {
                    pb.step();
                }
            }).sum();
        }

        System.out.printf("Removed %d uninhabited chunks%n", totalRemovedChunks);

        long originalSize = inputFile.calculateSize();
        long newSize = outputPath.calculateSize();

        System.out.println();
        System.out.println("Original World Size: " + FileUtil.humanReadableByteCountSI(originalSize));
        System.out.println("Purged World Size: " + FileUtil.humanReadableByteCountSI(newSize));
    }

    @SneakyThrows
    public static int purgeChunks(File mcaFile) {
        RegionFile regionFile = new RegionFile(mcaFile);
        RegionFile target = new RegionFile();

        int removedChunks = 0;

        for (int cx = 0; cx < RegionUtil.REGION_WIDTH; cx++) {
            for (int cz = 0; cz < RegionUtil.REGION_WIDTH; cz++) {
                RegionChunk regionChunk = regionFile.getChunkDataAt(cx, cz);

                if (regionChunk == null) {
                    continue;
                }

                CompoundTag chunkData = regionFile.loadChunk(regionChunk);
                AnvilChunk chunk = AnvilChunk.withTag(chunkData);

                // 60 seconds and no Tile Entities
                if (chunk.getInhabitedTime() > 60 * 20 || chunk.getTileEntities().size() > 0) {
                    target.addChunk(cx, cz, regionChunk);
                } else {
                    ++removedChunks;
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(mcaFile)) {
            target.write(fos);
        }

        return removedChunks;
    }
}
