package me.shawlaf.mcworldtool;

import me.shawlaf.mcworldtool.tasks.TaskPurgeWorld;
import org.fusesource.jansi.AnsiConsole;

public class Program {

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

//        TaskPurgeWorld.purgeChunks(new File("D:\\Users\\flori\\Projekte\\Minecraft\\Tools\\mcworldtool\\testdata\\r.2.2.mca"));

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
