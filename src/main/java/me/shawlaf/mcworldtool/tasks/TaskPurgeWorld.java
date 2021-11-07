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
import me.shawlaf.mcworldtool.util.MultithreadUtil;

import java.io.File;
import java.io.FileOutputStream;

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

        File[] mcaFiles = DirectoryTraversal.discoverWhere(outputPath,
                f -> f.getName().endsWith(".mca") && "region".equals(f.getParentFile().getAbsoluteFile().getName())
        ).toArray(File[]::new);

        int nThreads = Runtime.getRuntime().availableProcessors();
        File[][] threadFiles = MultithreadUtil.splitTaskForNThreads(File.class, nThreads, mcaFiles, File[]::new, File[][]::new);

        Thread[] threads = new Thread[nThreads];

        for (int i = 0; i < nThreads; i++) {
            final int finalI = i;

            threads[i] = new Thread(() -> {
                for (File regionFile : threadFiles[finalI]) {
                    try {
                        purgeChunks(regionFile);
                    } catch (Exception e) {
                        System.out.printf("Failed to purge Chunks from %s: %s%n", regionFile.getName(), e.getMessage());
                    }
                }
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long originalSize = inputFile.calculateSize();
        long newSize = outputPath.calculateSize();

        System.out.println();
        System.out.println("Original World Size: " + FileUtil.humanReadableByteCountSI(originalSize));
        System.out.println("Purged World Size: " + FileUtil.humanReadableByteCountSI(newSize));
    }

    @SneakyThrows
    public static void purgeChunks(File mcaFile) {
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

        System.out.printf("Purged %d Chunks from %s%n", removedChunks, mcaFile.getAbsolutePath());
    }
}
