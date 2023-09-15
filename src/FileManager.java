import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileManager {
    private static FileManager instance;
    private final Communication communication = Communication.getInstance();
    private Long positionInDataFile = -1L;

    private Long readPages = 0L;
    private Long wrotePages = 0L;
    public List<String> dataBlockBuffer = new ArrayList<>();

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    public void printDiskOperations() {
        System.out.println("---------------------------------");
        System.out.println("DISK OPERATIONS");
        communication.say("Read pages: " + readPages);
        communication.say("Wrote pages: " + wrotePages);
        readPages = 0L;
        wrotePages = 0L;
    }

    // test file purpose
    public List<String> getWordsFromFile(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Scanner scanner = new Scanner(fileInputStream);

            List<String> words = new ArrayList<>();

            while (scanner.hasNext()) {
                String word = scanner.next();
                words.add(word);
            }

            scanner.close();
            fileInputStream.close();

            return words;

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public void clearFile(File recordsFile) {
        try (FileOutputStream fos = new FileOutputStream(recordsFile, false)) {
            fos.getChannel().truncate(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Long writeParametersToFile(File file, String stringToFile) {
        dataBlockBuffer.add(stringToFile);
        if (dataBlockBuffer.size() == Consts.DATA_FILE_RECORDS_NUMBER_IN_BLOCK.getValue()) {
            flushDataBlockBuffer(file);
        }

        return ++positionInDataFile;
    }

    public void flushDataBlockBuffer(File file) {
        wrotePages++;
        if (dataBlockBuffer.size() == 0) {
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            StringBuilder sb = new StringBuilder();
            for (String dataRecord : dataBlockBuffer) {
                sb.append(dataRecord);
            }
            fileWriter.write(sb.toString());
            fileWriter.close();

            dataBlockBuffer = new ArrayList<>();

        } catch (IOException e) {
            communication.say("Writing to file unable.\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public void writeNodePageToFile(File indexFile, NodePage nodePage) {
        wrotePages++;
        String stringToFile = nodePage.serializeToFile();

        byte[] bytesToWrite = stringToFile.getBytes(StandardCharsets.UTF_8);

        try (RandomAccessFile raf = new RandomAccessFile(indexFile, "rw")) {
            raf.seek(nodePage.getPositionInIndex() * Consts.NODE_PAGE_BYTES_SIZE.getValue());
            raf.write(bytesToWrite);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NodePage getNodePage(File indexFile, Long indexPosition) {
        byte[] indexPage = new byte[Consts.NODE_PAGE_BYTES_SIZE.getValue()];
        int pageBytesRead = setPageBlock(indexFile, indexPosition * Consts.NODE_PAGE_BYTES_SIZE.getValue(), indexPage);
        if (pageBytesRead != Consts.NODE_PAGE_BYTES_SIZE.getValue()) {
            communication.say("Something went wrong with getting node page from file!");
        }

        return mapToNodePage(indexPosition, indexPage);
    }

    //
    public void printDataFile(File dataFile) throws IOException {
        communication.say("");
        communication.say("DATA FILE");

        flushDataBlockBuffer(dataFile);
        communication.setDataFileRecordPosition(0);
        long bytesPosition = 0;
        byte[] block = new byte[Consts.DATA_FILE_BLOCK_SIZE_IN_BYTES.getValue()];
        while (true) {
            int bytesRead = setPageBlock(
                    dataFile,
                    bytesPosition,
                    block
            );
            if (bytesRead == 0) {
                communication.say("Exception in getting next block!");
                break;
            } else if (bytesRead == -1) {
                break;
            }

            processDataFileBlock(
                    block,
                    bytesRead,
                    parameters -> communication.sayDataFileRecordParameters(formatParametersToPrint(parameters))
            );
            bytesPosition += bytesRead;
        }
    }

    public String formatParametersToPrint(Long[] parameters) {
        StringBuilder sb = new StringBuilder();
        for (Long parameter : parameters) {
            sb.append(parameter).append(" ");
        }

        return sb.toString();
    }

    public void printIndexFile(File indexFile, Long rootPosition) throws IOException {
        communication.say("");
        communication.say("INDEX FILE - root position: " + rootPosition);

        communication.setIndexFilePageNumberPosition(0);
        long bytePosition = 0;
        byte[] page = new byte[Consts.NODE_PAGE_BYTES_SIZE.getValue()];
        while (true) {
            int bytesRead = setPageBlock(
                    indexFile,
                    bytePosition,
                    page
            );
            if (bytesRead == 0) {
                communication.say("Exception in getting next page!");
                break;
            } else if (bytesRead == -1) {
                break;
            }

            // comes from architecture
            int fieldCharsNumber = Consts.INDEX_PAGE_FIELD_CHARS_NUMBER.getValue();
            int nextRecordPos = 2 * fieldCharsNumber;
            StringBuilder sb = new StringBuilder();
            for (int srcPos = 0; srcPos < bytesRead; srcPos += fieldCharsNumber) {

                Long field = getNodePageField(page, srcPos);

                if (srcPos == nextRecordPos) {
                    Long nextField = getNodePageField(page, srcPos + fieldCharsNumber);

                    if (nextField == Consts.EMPTY_VALUE.getValue()) {
                        sb.append("(_,_) "); // record (x, a) -> ... EMPTY EMPTY ...
                    } else {
                        srcPos += fieldCharsNumber;
                        sb.append("(").append(field).append(",").append(nextField).append(") ");
                    }
                    nextRecordPos += 3 * fieldCharsNumber;

                } else if (field == Consts.FILE_NULL.getValue()) {
                    sb.append("_ ");

                } else if (field != Consts.EMPTY_VALUE.getValue()) {
                    // skip address in data field if it was empty, because it was printed in first 'if'
                    sb.append(field).append(" ");
                }
            }
            communication.sayIndexFileNodePage(sb.toString());

            bytePosition += bytesRead;
        }
    }

    public String getDataRecord(File dataFile, Long position) {
        Long blockBytesPosition = (position / Consts.DATA_FILE_RECORDS_NUMBER_IN_BLOCK.getValue())
                * Consts.DATA_FILE_BLOCK_SIZE_IN_BYTES.getValue();
        long positionInBlock = position % Consts.DATA_FILE_RECORDS_NUMBER_IN_BLOCK.getValue();
        byte[] block = new byte[Consts.DATA_FILE_BLOCK_SIZE_IN_BYTES.getValue()];
        int bytesRead = setPageBlock(dataFile, blockBytesPosition, block);

        List<Long[]> dataRecordsList = new ArrayList<>();
        processDataFileBlock(
                block,
                bytesRead,
                dataRecordsList::add
        );

        return formatParametersToPrint(dataRecordsList.get((int) positionInBlock));
    }

    private int setPageBlock(
            File file,
            Long bytePosition,
            byte[] pageBlock
    ) {
        readPages++;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(bytePosition);

            return raf.read(pageBlock);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private NodePage mapToNodePage(Long indexPosition, byte[] indexPage) {
        int fieldCharsNumber = Consts.INDEX_PAGE_FIELD_CHARS_NUMBER.getValue();
        NodePage nodePage = new NodePage(indexPosition);

        int srcPos = 0;
        nodePage.setAncestorPositionInIndex(getNodePageField(indexPage, srcPos));

        srcPos += fieldCharsNumber;
        while (srcPos + fieldCharsNumber < indexPage.length) {
            Record record = new Record(indexPosition);

            // right child of previous record is the same as left child of next
            record.setLeftChildPositionInIndex(getNodePageField(indexPage, srcPos));

            srcPos += fieldCharsNumber;
            record.setKey(getNodePageField(indexPage, srcPos));

            srcPos += fieldCharsNumber;
            record.setPositionInDataFile(getNodePageField(indexPage, srcPos));

            srcPos += fieldCharsNumber;
            record.setRightChildPositionInIndex(getNodePageField(indexPage, srcPos));

            nodePage.addRecordAtBack(record);
        }

        return nodePage;
    }

    private Long getNodePageField(byte[] indexPage, int srcPos) {
        byte[] field = new byte[Consts.INDEX_PAGE_FIELD_CHARS_NUMBER.getValue()];
        System.arraycopy(indexPage, srcPos, field, 0, Consts.INDEX_PAGE_FIELD_CHARS_NUMBER.getValue());
        return Long.parseLong(
                new String(
                        field,
                        0,
                        Consts.INDEX_PAGE_FIELD_CHARS_NUMBER.getValue(),
                        StandardCharsets.UTF_8
                )
        );
    }

    private void processDataFileBlock(byte[] block, int bytesRead, Consumer<Long[]> actionOnRecordParameters) {
        for (int i = 0; i < bytesRead; i += Consts.DATA_FILE_RECORD_SIZE_IN_BYTES.getValue()) {
            int endIndex = Math.min(i + Consts.DATA_FILE_RECORD_SIZE_IN_BYTES.getValue(), bytesRead);
            byte[] record = new byte[endIndex - i];
            System.arraycopy(block, i, record, 0, endIndex - i);

            actionOnRecordParameters.accept(getRecordParameters(record));
        }
    }

    private Long[] getRecordParameters(byte[] record) {
        Long[] recordParameters = new Long[Consts.DATA_FILE_RECORD_SIZE_IN_BYTES.getValue() / Consts.PARAMETER_SIZE_IN_BYTES.getValue()];
        int k = 0;
        for (int i = 0; i < Consts.DATA_FILE_RECORD_SIZE_IN_BYTES.getValue(); i += Consts.PARAMETER_SIZE_IN_BYTES.getValue()) {
            int endIndex = Math.min(i + Consts.PARAMETER_SIZE_IN_BYTES.getValue(), Consts.DATA_FILE_RECORD_SIZE_IN_BYTES.getValue());
            byte[] parameter = new byte[endIndex - i];
            System.arraycopy(record, i, parameter, 0, endIndex - i);

            recordParameters[k++] = Long.parseLong(
                    new String(
                            parameter,
                            0,
                            Consts.PARAMETER_SIZE_IN_BYTES.getValue(),
                            StandardCharsets.UTF_8
                    )
            );
        }

        return recordParameters;
    }
}
