package me.shawlaf.mcworldtool;

import me.shawlaf.mcworldtool.tasks.TaskPurgeWorld;
import org.fusesource.jansi.AnsiConsole;

public class Program {

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        try {
            McWorldToolOptions options = new McWorldToolOptions();
            options.parse(args);

            switch (options.getCommand()) {
                case PURGE -> TaskPurgeWorld.run(options);
            }

        } finally {
            AnsiConsole.systemUninstall();
        }
    }

}
