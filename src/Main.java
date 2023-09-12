import java.io.File;

public class Main {
    private static final File dataFile = new File("data_file.txt");
    private static final File indexFile = new File("index_file.txt");

    public static void main(String[] args) {
        Communication communication = Communication.getInstance();
        communication.welcome();

        FileManager fileManager = FileManager.getInstance();
        fileManager.clearFile(indexFile);
        fileManager.clearFile(dataFile);

        RecordsSource recordsSource = communication.whatRecordsSource();
        if (recordsSource == RecordsSource.UNKNOWN) {
            communication.somethingWentWrong("Records source is unknown.");
            return;
        }

        BTree bTree = BTree.getInstance(indexFile, dataFile, recordsSource);
        bTree.build();



        communication.closeScanner();
    }
}