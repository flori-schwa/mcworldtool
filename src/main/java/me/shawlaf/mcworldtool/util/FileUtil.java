package me.shawlaf.mcworldtool.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;

@UtilityClass
public class FileUtil {

    @NotNull
    public File child(File self, String name) {
        return new File(self, name);
    }

    @SneakyThrows
    @NotNull
    public File requiredChild(File self, String name) {
        File file = child(self, name);

        if (!file.exists()) {
            throw new FileNotFoundException("File %s does not exist".formatted(file.getAbsolutePath()));
        }

        return file;
    }

    @SneakyThrows
    public void copyTo(File self, File target) {
        Objects.requireNonNull(self);
        Objects.requireNonNull(target);

        byte[] buffer = new byte[4096];
        int read;

        try (FileInputStream in = new FileInputStream(self)) {
            try (FileOutputStream out = new FileOutputStream(target)) {
                while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }
    }

    @SneakyThrows
    public void ensureMkdir(File self) {
        if (!self.mkdir()) {
            throw new IOException("Failed to create directory: %s".formatted(self.getAbsolutePath()));
        }
    }

    public void copyDirectoryRecursively(File sourceDirectory, File targetDirectory) {
        Objects.requireNonNull(sourceDirectory);
        Objects.requireNonNull(targetDirectory);

        if (!targetDirectory.exists()) {
            ensureMkdir(targetDirectory);
        }

        File[] files = sourceDirectory.listFiles();

        if (files == null) {
            return;
        }

        for (File source : files) {
            File target = child(targetDirectory, source.getName());

            if (source.isDirectory()) {
                ensureMkdir(target);

                copyDirectoryRecursively(source, target);
            } else {
                copyTo(source, target);
            }
        }
    }

}
