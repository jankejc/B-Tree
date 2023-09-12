import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecordGenerator {

    private static RecordGenerator instance;


    public static RecordGenerator getInstance() {
        if (instance == null) {
            instance = new RecordGenerator();
        }
        return instance;
    }

    /**
     * @return True if index and data file are filled.
     */
//    public Boolean (
//            Communication communication,
//            RecordsSource recordsSource,
//            File indexFile,
//            File dataFile
//    ) {
//        switch (recordsSource) {
//            case MANUALLY -> {
//                Long recordsNumber = communication.recordsNumber();
//                if (!indexFile.exists()) {
//                    communication.say("File doesn't exist.");
//                    return false;
//                }
//                return generateManually(mergeTape.getFile(), recordsNumber);
//            }
//            case TEST_FILE -> {
////                String testFilePath = CommunicationManager.testFilePath();
////
////                mergeTape = new Tape(testFilePath, TapeType.MERGE);
////
////                if (!mergeTape.getFile().exists()) {
////                    CommunicationManager.say("File doesn't exist.");
////                    return false;
////                } else {
////                    return true;
////                }
//            }
//            default -> {
//                return false;
//            }
//        }
//    }


    /**
     * @return True if generating was successful.
     */
//    public boolean generateManually(File recordsFile, Long recordsNumber) {
//        for (long i = 0L; i < recordsNumber; i++) {
//            String[] numbersInRecord = CommunicationManager.getRecord();
//            StringBuilder recordToFile = new StringBuilder();
//            for (String number : numbersInRecord) {
//                recordToFile.append(formatNumber(number));
//            }
//
//            // Write record by record.
//            if (!FileManager.writeToFile(recordsFile, recordToFile.toString())) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    private static String formatNumber(String numberString) {
//        return String.format("%0" + FIXED_DIGITS_NUMBER + "d", Long.parseLong(numberString));
//    }
//
//    // todo: check if its work
//    public static boolean convertTestFile(File recordsFile, File testFile) throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader(testFile));
//        String line;
//        int count = 0;
//        StringBuilder recordToFile = new StringBuilder();
//
//        while ((line = reader.readLine()) != null) {
//            String[] numbers = line.split(" ");
//
//            for (String number : numbers) {
//                recordToFile.append(formatNumber(number));
//                count++;
//
//                if (count == PARAMETER_NUMBERS) {
//                    // Write record by record.
//                    if (!FileManager.writeToFile(recordsFile, recordToFile.toString())) {
//                        return false;
//                    }
//
//                    recordToFile = new StringBuilder();
//                    count = 0;
//                }
//            }
//        }
//
//        return true;
//    }
}
