import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

public class BTree {
    private static BTree instance;
    private NodePage root;
    private final File indexFile;
    private final File dataFile;
    private final RecordsSource recordsSource;
    private final Communication communication = Communication.getInstance();

    private BTree(File indexFile,
                  File dataFile,
                  RecordsSource recordsSource
    ) {
        this.indexFile = indexFile;
        this.dataFile = dataFile;
        this.recordsSource = recordsSource;
    }

    public void build() {
        if (recordsSource == RecordsSource.MANUALLY) {
            interactBuild();
        } else {
            // TODO: test file
        }
    }

    private void interactBuild() {
        Command command;
        do {
            command = communication.whatToDo();
            execute(command);
        } while(command.getOperation() == Operation.FINISH);
    }

    private void execute(Command command) {
        switch (command.getOperation()) {
            case ADD -> add(command);
            case DELETE -> {
                // TODO
            }
            default -> {}
        }
    }

    /**
     * @return True if record was added.
     */
    private boolean add(Command command) {
        communication.say("ADDED " + command.getKey());
        return true;
    }


    public static BTree getInstance(File indexFile,
                                    File dataFile,
                                    RecordsSource recordsSource
    ) {
        if (instance == null) {
            instance = new BTree(indexFile, dataFile, recordsSource);
        }
        return instance;
    }
}
