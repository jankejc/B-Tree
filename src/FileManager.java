import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileManager {
//    private static final int PARAMETER_SIZE_IN_BYTES = RecordsGenerator.FIXED_DIGITS_NUMBER;
//    private static final int RECORD_SIZE_IN_BYTES = RecordsGenerator.PARAMETER_NUMBERS * PARAMETER_SIZE_IN_BYTES;
//    private static final int RECORDS_IN_BLOCK = 4;
//    private static final int BLOCK_SIZE_IN_BYTES = RECORDS_IN_BLOCK * RECORD_SIZE_IN_BYTES;

//    public static int phase = 1;

    private static FileManager instance;


    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    public void clearFile(File recordsFile) {
        try (FileOutputStream fos = new FileOutputStream(recordsFile, false)) {
            fos.getChannel().truncate(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public boolean writeToFile(File recordsFile, String stringToFile) {
//        try {
//            FileWriter fileWriter = new FileWriter(recordsFile, true);
//            fileWriter.write(stringToFile);
//            fileWriter.close();
//
//            return true;
//
//        } catch (IOException e) {
//            CommunicationManager.say("Writing to file unable.\n" + Arrays.toString(e.getStackTrace()));
//            return false;
//        }
//    }
//
//    public void printFile(Tape tape) throws IOException {
//        CommunicationManager.say("---------------------------------");
//        CommunicationManager.say("RECORDS FILE");
//
//        byte[] block = new byte[BLOCK_SIZE_IN_BYTES];
//        while (true) {
//            int bytesRead = setNextBlock(tape, block);
//            if (bytesRead == 0) {
//                CommunicationManager.say("Exception in getting next block!");
//                break;
//            } else if (bytesRead == -1) {
//                tape.setPositionInFile(0L);
//                break;
//            }
//
//            processBlock(
//                    block,
//                    bytesRead,
//                    parameters -> {
//                        CommunicationManager.say(new Record(parameters).getValue().toString());
//                    }
//            );
//        }
//    }
//
//    private int setNextBlock(
//            Tape tape,
//            byte[] block
//    ) {
//        try (RandomAccessFile raf = new RandomAccessFile(tape.getFile(), "r")) {
//            raf.seek(tape.getPositionInFile());
//
//            int bytesRead = raf.read(block);
//            tape.setPositionInFile(tape.getPositionInFile() + bytesRead);
//
//            return bytesRead;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return 0;
//    }
//
//    private void processBlock(byte[] block, int bytesRead, Consumer<Long[]> actionOnRecordParameters) {
//        for (int i = 0; i < bytesRead; i += RECORD_SIZE_IN_BYTES) {
//            int endIndex = Math.min(i + RECORD_SIZE_IN_BYTES, bytesRead);
//            byte[] record = new byte[endIndex - i];
//            System.arraycopy(block, i, record, 0, endIndex - i);
//
//            actionOnRecordParameters.accept(getRecordParameters(record));
//        }
//    }
//
//    private Long[] getRecordParameters(byte[] record) {
//        Long[] recordParameters = new Long[RECORD_SIZE_IN_BYTES / PARAMETER_SIZE_IN_BYTES];
//        int k = 0;
//        for (int i = 0; i < RECORD_SIZE_IN_BYTES; i += PARAMETER_SIZE_IN_BYTES) {
//            int endIndex = Math.min(i + PARAMETER_SIZE_IN_BYTES, RECORD_SIZE_IN_BYTES);
//            byte[] number = new byte[endIndex - i];
//            System.arraycopy(record, i, number, 0, endIndex - i);
//
//            try {
//                recordParameters[k++] = Long.parseLong(new String(number, 0, PARAMETER_SIZE_IN_BYTES, "UTF-8"));
//            } catch (UnsupportedEncodingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        return recordParameters;
//    }
}
