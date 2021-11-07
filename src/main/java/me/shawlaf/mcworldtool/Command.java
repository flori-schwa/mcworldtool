package me.shawlaf.mcworldtool;

import lombok.Getter;

public enum Command {

    PURGE("purge");

    @Getter
    private final String name;

    Command(String name) {
        this.name = name;
    }
}
