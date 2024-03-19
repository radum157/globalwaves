package main;

import app.admin.Admin;
import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import databases.UserDatabase;
import fileio.commands.statistics.EndProgramResponse;
import fileio.input.LibraryInput;
import fileio.commands.handler.CommandHandler;
import fileio.commands.wrapper.CommandWrapper;
import databases.Library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    static final String LIBRARY_PATH = CheckerConstants.TESTS_PATH + "library/library.json";

    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("library")) {
                continue;
            }

            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePathInput for input file
     * @param filePathOutput for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePathInput,
                              final String filePathOutput) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LibraryInput library = objectMapper.readValue(new File(LIBRARY_PATH), LibraryInput.class);

        ArrayNode outputs = objectMapper.createArrayNode();

        /* !!! The clear method should be removed in production !!! */
        Library.getInstance().clear();
        UserDatabase.getInstance().clear();
        EndProgramResponse.clearSongs();

        Admin.addToDatabase(library);

        Library libraryWrapper = Library.getInstance();
        CommandWrapper[] inputCommands = objectMapper.readValue(
                new File(CheckerConstants.TESTS_PATH + filePathInput),
                CommandWrapper[].class
        );

        for (CommandWrapper command : inputCommands) {
            outputs.add(
                    CommandHandler.executeCommand(command).toNode()
            );
        }

        EndProgramResponse endProgramResponse = new EndProgramResponse("endProgram");
        endProgramResponse.getResponse(libraryWrapper, UserDatabase.getInstance(),
                inputCommands[inputCommands.length - 1]);
        outputs.add(endProgramResponse.toNode());

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePathOutput), outputs);
    }
}
