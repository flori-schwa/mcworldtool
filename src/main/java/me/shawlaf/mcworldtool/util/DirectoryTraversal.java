package me.shawlaf.mcworldtool.util;

import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class DirectoryTraversal {

    @Getter
    private final File currentDirectory;
    private final File[] children;
    private int index = -1;

    private DirectoryTraversal pop = null;

    public DirectoryTraversal(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " is not a directory");
        }

        this.currentDirectory = directory;
        this.children = directory.listFiles();
    }

    public File getFile() {
        if (index >= children.length) {
            return null;
        }

        return children[index];
    }

    private File nextFile() {
        return (index + 1) == children.length ? null : children[++index];
    }

    public DirectoryTraversal next() {
        File nextFile = nextFile();

        if (nextFile == null) {
            return Optional.ofNullable(this.pop).map(DirectoryTraversal::next).orElse(null);
        } else if (nextFile.isDirectory()) {
            DirectoryTraversal next = new DirectoryTraversal(nextFile);
            next.pop = this;

            return next.next();
        } else {
            return this;
        }
    }

    public static List<File> discoverWhere(File root, Predicate<File> predicate) {
        DirectoryTraversal traversal = new DirectoryTraversal(root).next();
        List<File> result = new ArrayList<>();

        if (traversal != null && traversal.getFile() != null) {
            do {
                File file = traversal.getFile();

                if (predicate.test(file)) {
                    result.add(file);
                }
            } while ((traversal = traversal.next()) != null);
        }

        return result;
    }

}
