package me.shawlaf.mcworldtool;

import lombok.SneakyThrows;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class McWorldToolOptions implements IMcWorldToolOptions {

    private final Options options;
    private final Option optInputFile;
    private final Option optCommand;

    private final CommandLineParser parser = new DefaultParser();
    private final HelpFormatter helpFormatter = new HelpFormatter();

    private CommandLine cmd;

    public McWorldToolOptions() {
        this.options = new Options();

        this.optInputFile = new Option("i", "input", true, "input file path");
        this.optInputFile.setRequired(true);

        this.optCommand = new Option("c", "command", true, "command to execute");
        this.optCommand.setRequired(true);

        this.options.addOption(this.optInputFile);
        this.options.addOption(this.optCommand);
    }

    public boolean parse(String... args) {
        try {
            this.cmd = parser.parse(this.options, args);

            return true;
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp();

            return false;
        }
    }

    public void printHelp() {
        this.helpFormatter.printHelp("MC World Tool", this.options);
    }

    @Override
    public File getInputPath() {
        assertParsed();

        return new File(cmd.getOptionValue(optInputFile.getLongOpt()));
    }

    @SneakyThrows
    @Override
    public Command getCommand() {
        assertParsed();

        String value = cmd.getOptionValue(this.optCommand.getLongOpt());

        for (Command command : Command.values()) {
            if (command.getName().equalsIgnoreCase(value)) {
                return command;
            }
        }

        throw new ParseException("Illegal command entered: %s, expected one of: %s".formatted(value, Arrays.stream(Command.values()).map(Command::getName).collect(Collectors.joining(", "))));
    }

    // region Private API

    private void assertParsed() {
        Objects.requireNonNull(this.cmd, "No Options have been parsed yet");
    }

    // endregion
}
