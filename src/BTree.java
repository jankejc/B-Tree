import java.io.File;
import java.io.IOException;
import java.util.*;

public class BTree {
    private static BTree instance;
    private Long rootPositionInIndex = (long) Consts.FILE_NULL.getValue();
    private final File indexFile;
    private final File dataFile;
    private final RecordsSource recordsSource;
    private final Communication communication = Communication.getInstance();
    private final FileManager fileManager = FileManager.getInstance();

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
            commandsFromTestFile(communication.testFilePath());
        }
    }

    private void interactBuild() {
        Command command;
        do {
            command = communication.whatToDo();
            execute(command);
        } while (command.getOperation() != Operation.FINISH);
    }

    private void commandsFromTestFile(String testFilePath) {
        List<Command> commands = Command.getCommandsFromFile(testFilePath);
        for (Command command : commands) {
            execute(command);
        }
        interactBuild();
    }

    private void execute(Command command) {
        switch (command.getOperation()) {
            case SEARCH -> {
                Record closestRecord = search(rootPositionInIndex, command.getKey(), null);
                if (closestRecord != null && closestRecord.getKey().equals(command.getKey())) {
                    communication.say("---------------------------------");
                    communication.say("SEARCH " + closestRecord.getKey());
                    printRecord(closestRecord);
                } else {
                    communication.say(command.getKey() + " doesn't exists");
                }
                fileManager.printDiskOperations();
            }
            case ORDERED_TRAVERSE -> {
                orderedTraverse(rootPositionInIndex);
                fileManager.printDiskOperations();
            }
            case ADD -> {
                add(command);
                if (communication.whetherPrintFiles()) {
                    printFiles();
                }
                fileManager.printDiskOperations();
            }
            default -> {
            }
        }
    }

    private void orderedTraverse(Long positionInIndex) {
        communication.say("---------------------------------");
        communication.say("ORDERED TRAVERSE");
        Record record = search(positionInIndex, (long) Consts.MIN_KEY.getValue(), null);
        if (record != null) {
            NodePage nodePage = fileManager.getNodePage(indexFile, record.getNodePagePositionInIndex());
            Record recordFromThePage = nodePage.findClosestRecord(record.getKey());
            traverse(recordFromThePage, nodePage, false, null);
        }
    }

    private void traverse(Record record, NodePage nodePage, Boolean wentUp, Record oldRecord) {
        int recordIndex = nodePage.getRecords().indexOf(record);
        int firstEmptyRecordIndex = nodePage.getRecords().indexOf(nodePage.findFirstEmptyRecord());
        if (firstEmptyRecordIndex == -1) {
            firstEmptyRecordIndex = nodePage.getRecords().size();
        }

        if (wentUp) {
            Record currentRecord = null;
            for (int i = 0; i < nodePage.getRecords().size(); i++) {
                if (nodePage.getRecords().get(i).getLeftChildPositionInIndex().equals(oldRecord.getNodePagePositionInIndex())) {
                    if (nodePage.getRecords().get(i).getPositionInDataFile() == Consts.EMPTY_VALUE.getValue()) {
                        currentRecord = nodePage.getRecords().get(i - 1);
                    } else {
                        currentRecord = nodePage.getRecords().get(i);
                    }
                    break;
                }
            }

            recordIndex = nodePage.getRecords().indexOf(currentRecord);

            if (currentRecord.getRightChildPositionInIndex() != Consts.FILE_NULL.getValue()) {
                if (currentRecord.getRightChildPositionInIndex().equals(oldRecord.getNodePagePositionInIndex())) {
                    if (nodePage.getAncestorPositionInIndex() == Consts.FILE_NULL.getValue()) {
                        // end of root
                        return;
                    }
                    NodePage newNodePage = fileManager.getNodePage(indexFile, nodePage.getAncestorPositionInIndex());
                    traverse(newNodePage.getRecords().getFirst(), newNodePage, true, currentRecord);
                } else {
                    printRecord(currentRecord);
                    NodePage newNodePage = fileManager.getNodePage(indexFile, currentRecord.getRightChildPositionInIndex());
                    traverse(newNodePage.getRecords().getFirst(), newNodePage, false, currentRecord);
                }
            } else if (recordIndex + 1 < firstEmptyRecordIndex) {
                // right sibling
                printRecord(currentRecord);
                traverse(nodePage.getRecords().get(recordIndex + 1), nodePage, false, currentRecord);

            } else if (currentRecord.getRightChildPositionInIndex() == Consts.FILE_NULL.getValue()) {
                if (nodePage.getAncestorPositionInIndex() == Consts.FILE_NULL.getValue()) {
                    // end of root
                    return;
                }
                NodePage newNodePage = fileManager.getNodePage(indexFile, nodePage.getAncestorPositionInIndex());
                traverse(newNodePage.getRecords().getFirst(), newNodePage, true, currentRecord);
            }
        } else if (record.getLeftChildPositionInIndex() != Consts.FILE_NULL.getValue()) {
            // go deep
            NodePage newNodePage = fileManager.getNodePage(indexFile, record.getRightChildPositionInIndex());
            traverse(newNodePage.getRecords().getFirst(), newNodePage, false, record);

        } else if (record.getLeftChildPositionInIndex() == Consts.FILE_NULL.getValue()) {
            if (recordIndex + 1 < firstEmptyRecordIndex) {
                // right sibling
                printRecord(record);
                traverse(nodePage.getRecords().get(recordIndex + 1), nodePage, false, record);

            } else if (record.getRightChildPositionInIndex() != Consts.FILE_NULL.getValue()) {
                NodePage newNodePage = fileManager.getNodePage(indexFile, record.getRightChildPositionInIndex());
                traverse(newNodePage.getRecords().getFirst(), newNodePage, false, record);

            } else if (record.getRightChildPositionInIndex() == Consts.FILE_NULL.getValue()) {
                printRecord(record);
                if (nodePage.getAncestorPositionInIndex() == Consts.FILE_NULL.getValue()) {
                    // end of root
                    return;
                }
                NodePage newNodePage = fileManager.getNodePage(indexFile, nodePage.getAncestorPositionInIndex());
                traverse(newNodePage.getRecords().getFirst(), newNodePage, true, record);
            }
        }
    }

    private void printRecord(Record record) {
        communication.say("key: " + record.getKey()
                + " data: " + fileManager.getDataRecord(dataFile, record.getPositionInDataFile()));
    }

    private void printFiles() {
        try {
            fileManager.printDataFile(dataFile);
        } catch (IOException e) {
            communication.somethingWentWrong(
                    "Error in reading from data file.\n" + Arrays.toString(e.getStackTrace())
            );
            return;
        }

        try {
            fileManager.printIndexFile(indexFile, rootPositionInIndex);
        } catch (IOException e) {
            communication.somethingWentWrong(
                    "Error in reading from index file.\n" + Arrays.toString(e.getStackTrace())
            );
        }
    }

    private void add(Command command) {
        Record closestRecord = search(rootPositionInIndex, command.getKey(), null);

        if (closestRecord != null && closestRecord.getKey().equals(command.getKey())) {
            communication.say(closestRecord.getKey() + " exists");
            return;
        }

        Long positionInDataFile = fileManager.writeParametersToFile(dataFile, command.getParametersToFile());
        write(
                closestRecord,
                new Record(command.getKey(), positionInDataFile)
        );
        for (int i = 0; i < NodePage.nodePagesPositionCounter; i++) {
            updatePagesAncestors(fileManager.getNodePage(indexFile, (long) i));
        }

        communication.say("---------------------------------");
        communication.say("ADDED " + command.getKey());
    }

    /**
     * @return closest record to the given key
     */
    private Record search(Long positionInIndex, Long key, Record previousRecord) {
        if (positionInIndex == Consts.FILE_NULL.getValue()) {
            return previousRecord;
        }

        NodePage nodePage = fileManager.getNodePage(indexFile, positionInIndex);
        Record closestRecord = nodePage.findClosestRecord(key);
        if (closestRecord.getKey().equals(key)) {
            return closestRecord;

        } else if (key < closestRecord.getKey()) {
            return search(closestRecord.getLeftChildPositionInIndex(), key, closestRecord);

        } else {
            return search(closestRecord.getRightChildPositionInIndex(), key, closestRecord);
        }
    }

    private void overwrite(NodePage nodePage, Record recordToOverwrite, Record newRecord) {
        nodePage.getRecords().add(nodePage.getRecords().indexOf(recordToOverwrite), newRecord);
        nodePage.getRecords().remove(recordToOverwrite);
        fileManager.writeNodePageToFile(indexFile, nodePage);
    }

    private Long write(Record closestRecord, Record newRecord) {
        if (closestRecord == null) {
            NodePage newRootPage = new NodePage();
            newRecord.setNodePagePositionInIndex(newRootPage.getPositionInIndex());
            newRootPage.addRecordAtBack(newRecord);
            newRootPage.refactorEmptyRecords();
            rootPositionInIndex = newRootPage.getPositionInIndex();
            fileManager.writeNodePageToFile(indexFile, newRootPage);

            return newRootPage.getPositionInIndex();
        }

        NodePage nodePage = fileManager.getNodePage(indexFile, closestRecord.getNodePagePositionInIndex());
        if (nodePage.canAdd()) {
            NodePage.addRecordInProperPlace(
                    nodePage,
                    closestRecord,
                    newRecord
            );
            nodePage.refactorEmptyRecords();
            fileManager.writeNodePageToFile(indexFile, nodePage);

            return nodePage.getPositionInIndex();

        } else {
            if (!compensation(nodePage, newRecord)) {
                split(
                        nodePage,
                        closestRecord,
                        newRecord
                );
            }
            return -10L;
        }
    }

    private boolean compensation(NodePage nodePage, Record newRecord) {
        if (nodePage.getAncestorPositionInIndex() == Consts.FILE_NULL.getValue()) {
            return false;
        }
        NodePage ancestorNodePage = fileManager.getNodePage(indexFile, nodePage.getAncestorPositionInIndex());
        Record recordByChildPosition = ancestorNodePage.findRecordByChildPosition(nodePage.getPositionInIndex());
        Record leftRecordByChildPosition = ancestorNodePage.getLeftSiblingRecord(recordByChildPosition);
        Record rightRecordByChildPosition = ancestorNodePage.getRightSiblingRecord(recordByChildPosition);

        // to check sibling pages I need to be on middle page between
        NodePage sibling;
        if (recordByChildPosition.getLeftChildPositionInIndex() != Consts.FILE_NULL.getValue()
                && recordByChildPosition.getLeftChildPositionInIndex().equals(nodePage.getPositionInIndex())
        ) {
            // if I'm left child
            // I can look for right child page of the current record or ...
            if (recordByChildPosition.getRightChildPositionInIndex() != Consts.FILE_NULL.getValue()) {
                sibling = fileManager.getNodePage(indexFile, recordByChildPosition.getRightChildPositionInIndex());
                if (sibling.canAdd()) {
                    rearrangeSiblingPages(nodePage, recordByChildPosition, newRecord, sibling, ancestorNodePage);
                    return true;
                }
            }

            // ... I can look for left child of the left sibling of the ancestor record
            if (leftRecordByChildPosition != null
                    && leftRecordByChildPosition.getLeftChildPositionInIndex() != Consts.FILE_NULL.getValue()
            ) {
                sibling = fileManager.getNodePage(indexFile, leftRecordByChildPosition.getLeftChildPositionInIndex());
                if (sibling.canAdd()) {
                    rearrangeSiblingPages(sibling, leftRecordByChildPosition, newRecord, nodePage, ancestorNodePage);
                    return true;
                }
            }

        } else if (recordByChildPosition.getRightChildPositionInIndex() != Consts.FILE_NULL.getValue()
                && recordByChildPosition.getRightChildPositionInIndex().equals(nodePage.getPositionInIndex())
        ) {
            // if I'm right child
            // I can look for left child page of the current record or ...
            if (recordByChildPosition.getLeftChildPositionInIndex() != Consts.FILE_NULL.getValue()) {
                sibling = fileManager.getNodePage(indexFile, recordByChildPosition.getLeftChildPositionInIndex());
                if (sibling.canAdd()) {
                    rearrangeSiblingPages(sibling, recordByChildPosition, newRecord, nodePage, ancestorNodePage);
                    return true;
                }
            }

            // ... I can look for right child of the right sibling of the ancestor record
            if (rightRecordByChildPosition != null
                    && rightRecordByChildPosition.getRightChildPositionInIndex() != Consts.FILE_NULL.getValue()
            ) {
                sibling = fileManager.getNodePage(indexFile, rightRecordByChildPosition.getRightChildPositionInIndex());
                if (sibling.canAdd()) {
                    rearrangeSiblingPages(nodePage, rightRecordByChildPosition, newRecord, sibling, ancestorNodePage);
                    return true;
                }
            }
        }

        return false;
    }

    private void rearrangeSiblingPages(
            NodePage leftNodePage,
            Record recordByChildPosition,
            Record newRecord,
            NodePage rightNodePage,
            NodePage ancestorNodePage
    ) {
        leftNodePage.removeEmptyRecords();
        rightNodePage.removeEmptyRecords();
        Record closestRecord;
        if (newRecord.getKey() > recordByChildPosition.getKey()) {
            closestRecord = rightNodePage.findClosestRecord(newRecord.getKey());
            NodePage.addRecordInProperPlace(rightNodePage, closestRecord, newRecord);
        } else {
            closestRecord = leftNodePage.findClosestRecord(newRecord.getKey());
            NodePage.addRecordInProperPlace(leftNodePage, closestRecord, newRecord);
        }

        // reset children from upper record
        recordByChildPosition.setLeftChildPositionInIndex((long) Consts.FILE_NULL.getValue());
        recordByChildPosition.setRightChildPositionInIndex((long) Consts.FILE_NULL.getValue());

        // I base on knowledge that there will be only bigger numbers.
        Record[] recordsArray = new Record[1 + rightNodePage.getRecords().size()];
        int i = 0;
        recordsArray[i++] = recordByChildPosition;
        for (Record record : rightNodePage.getRecords()) {
            recordsArray[i++] = record;
        }

        Record middleRecord = NodePage.splitRecords(
                leftNodePage,
                rightNodePage,
                leftNodePage.getRecords().getLast(),
                recordsArray
        );

        fileManager.writeNodePageToFile(indexFile, leftNodePage);
        fileManager.writeNodePageToFile(indexFile, rightNodePage);
        fileManager.flushDataBlockBuffer(dataFile, true);

        middleRecord.setNodePagePositionInIndex(ancestorNodePage.getPositionInIndex());
        overwrite(ancestorNodePage, recordByChildPosition, middleRecord);
    }

    private NodePage split(NodePage nodePage, Record closestRecord, Record newRecord) {
        NodePage newNodePage = new NodePage();
        Record middleRecord = NodePage.splitRecords(nodePage, newNodePage, closestRecord, newRecord);

        if (nodePage.getAncestorPositionInIndex() == (long) Consts.FILE_NULL.getValue()) {
            // it is root page
            Long newPositionInIndex = write(null, middleRecord);

            nodePage.setAncestorPositionInIndex(newPositionInIndex);
            newNodePage.setAncestorPositionInIndex(newPositionInIndex);

            fileManager.writeNodePageToFile(indexFile, nodePage);
            fileManager.writeNodePageToFile(indexFile, newNodePage);
            fileManager.flushDataBlockBuffer(dataFile, true);

            return newNodePage;

        } else {
            NodePage ancestorNodePage = fileManager.getNodePage(indexFile, nodePage.getAncestorPositionInIndex());
            Record newClosestRecord = ancestorNodePage.findClosestRecord(middleRecord.getKey());

            write(newClosestRecord, middleRecord);

            nodePage.setAncestorPositionInIndex(middleRecord.getNodePagePositionInIndex());
            newNodePage.setAncestorPositionInIndex(middleRecord.getNodePagePositionInIndex());

            fileManager.writeNodePageToFile(indexFile, nodePage);
            fileManager.writeNodePageToFile(indexFile, newNodePage);
            fileManager.flushDataBlockBuffer(dataFile, true);

            return newNodePage;
        }
    }

    private void updatePagesAncestors(NodePage nodePage) {
        for (Record record : nodePage.getRecords()) {
            if (record.getLeftChildPositionInIndex() != Consts.FILE_NULL.getValue()) {
                NodePage childPage = fileManager.getNodePage(indexFile, record.getLeftChildPositionInIndex());
                childPage.setAncestorPositionInIndex(nodePage.getPositionInIndex());
                fileManager.writeNodePageToFile(indexFile, childPage);
                fileManager.flushDataBlockBuffer(dataFile, true);
            }

            if (nodePage.ifLastElement(nodePage.getRecords().indexOf(record))
                    && record.getRightChildPositionInIndex() != Consts.FILE_NULL.getValue()
            ) {
                NodePage childPage = fileManager.getNodePage(indexFile, record.getRightChildPositionInIndex());
                childPage.setAncestorPositionInIndex(nodePage.getPositionInIndex());
                fileManager.writeNodePageToFile(indexFile, childPage);
                fileManager.flushDataBlockBuffer(dataFile, true);
                break;
            }
        }
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
