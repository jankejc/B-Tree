import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Objects;

@Getter
@Setter
public class NodePage {
    public static Long nodePagesPositionCounter = 0L;
    private @NonNull Long positionInIndex;
    private Long ancestorPositionInIndex = (long) Consts.FILE_NULL.getValue();
    private @NonNull LinkedList<Record> records = new LinkedList<>(); // size is always RECORDS_NUMBER_IN_NODE_PAGE <- empty elements are added


    // Use for every new NodePage!
    public NodePage() {
        this.positionInIndex = nodePagesPositionCounter++;
    }

    public NodePage(Long positionInIndex) {
        this.positionInIndex = positionInIndex;
    }

    /**
     * @return record that is equal or the closest to key I'm looking for.
     */
    public Record findClosestRecord(Long key) {
        Record closestRecord = null;
        long difference = 0;
        for (Record record : records) {
            // skip empty records
            if (record.getPositionInDataFile() == Consts.EMPTY_VALUE.getValue()) {
                continue;
            }

            long newDifference = Math.abs(key - record.getKey());

            if (closestRecord == null) {
                closestRecord = record;
                difference = newDifference;

            } else if (newDifference < difference) {
                closestRecord = record;
                difference = newDifference;
            }
        }

        return closestRecord;
    }

    public boolean canAdd() {
        return findFirstEmptyRecord() != null;
    }

    public Record findFirstEmptyRecord() {
        for (Record record : records) {
            // this is the best field to show in file that the record is empty and not null
            if (record.getPositionInDataFile() == Consts.EMPTY_VALUE.getValue()) {
                return record;
            }
        }

        return null;
    }

    public void removeEmptyRecords() {
        int i = 0;
        while (i < records.size()) {
            Record currentRecord = records.get(i);
            if (currentRecord.getPositionInDataFile() == Consts.EMPTY_VALUE.getValue()) {
                records.remove(currentRecord);
            } else {
                i++;
            }
        }
    }

    public void refactorEmptyRecords() {
        // remove surplus empty records
        while (records.size() > Consts.RECORDS_NUMBER_IN_NODE_PAGE.getValue()) {
            records.remove(findFirstEmptyRecord());
        }

        // add empty records that lacks
        while (records.size() < Consts.RECORDS_NUMBER_IN_NODE_PAGE.getValue()) {
            records.add(new Record(
                            (long) Consts.EMPTY_VALUE.getValue(),
                            (long) Consts.EMPTY_VALUE.getValue()
                    )
            );
        }
    }

    public static void addRecordInProperPlace(NodePage nodePage, Record closestRecord, Record newRecord) {
        int closestRecordIndex = -1;
        for (Record record : nodePage.records) {
            if (Objects.equals(record.getKey(), closestRecord.getKey())) {
                closestRecordIndex = nodePage.records.indexOf(record);
                break;
            }
        }

        newRecord.setNodePagePositionInIndex(closestRecord.getNodePagePositionInIndex());

        if (closestRecord.getKey() < newRecord.getKey()) {
            nodePage.records.get(closestRecordIndex).setRightChildPositionInIndex(newRecord.getLeftChildPositionInIndex());
            if (closestRecordIndex + 1 < nodePage.records.size()) {
                nodePage.records.get(closestRecordIndex + 1).setRightChildPositionInIndex(
                        // left child from right sibling must be equal to new page
                        newRecord.getLeftChildPositionInIndex()
                );
            }
            nodePage.records.add(closestRecordIndex + 1, newRecord);

        } else if (closestRecord.getKey() > newRecord.getKey()) {
            nodePage.records.get(closestRecordIndex).setLeftChildPositionInIndex(newRecord.getRightChildPositionInIndex());
            if (closestRecordIndex - 1 >= 0) {
                nodePage.records.get(closestRecordIndex - 1).setLeftChildPositionInIndex(
                        // right child from left sibling must be equal to new page
                        newRecord.getRightChildPositionInIndex()
                );
            }
            nodePage.records.add(closestRecordIndex, newRecord);
        }
        // There is more records than it should be.
    }

    public void addRecordAtBack(Record record) {
        records.add(record);
    }

    public String serializeToFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(formatFieldToFile(ancestorPositionInIndex));

        boolean toggle = false;
        for (int i = 0; i < records.size(); i++) {
            if (!toggle) {
                sb.append(formatFieldToFile(records.get(i).getLeftChildPositionInIndex()));
            } else {
                toggle = false;
            }
            sb.append(formatFieldToFile(records.get(i).getKey()));
            sb.append(formatFieldToFile(records.get(i).getPositionInDataFile()));

            if (ifLastElement(i)) {
                sb.append(formatFieldToFile(records.get(i).getRightChildPositionInIndex()));
                toggle = true;
            }
        }

        return sb.toString();
    }

    public boolean ifLastElement(int i) {
        // if the next element is empty and current is not
        // or if it is the last element
        return (i + 1 < records.size()
                && records.get(i).getPositionInDataFile() != Consts.EMPTY_VALUE.getValue()
                && records.get(i + 1).getPositionInDataFile() == Consts.EMPTY_VALUE.getValue())
                || i + 1 == Consts.RECORDS_NUMBER_IN_NODE_PAGE.getValue();
    }

    private String formatFieldToFile(Long field) {
        return String.format("%0" + Consts.INDEX_PAGE_FIELD_CHARS_NUMBER.getValue() + "d", field);
    }

    public static Record splitRecords(NodePage originalPage, NodePage secondPage, Record closestRecord, Record... newRecords) {
        Record tempClosestRecord = closestRecord;
        for (Record newRecord : newRecords) {
            addRecordInProperPlace(originalPage, tempClosestRecord, newRecord);
            tempClosestRecord = originalPage.records.getLast();
        }
        // there are extra records on list - no empty ones

        int middleRecordIndex = originalPage.records.size() / 2;
        Record middleRecord = originalPage.records.get(middleRecordIndex);
        middleRecord.setLeftChildPositionInIndex(originalPage.positionInIndex);
        middleRecord.setRightChildPositionInIndex(secondPage.positionInIndex);

        secondPage.records = new LinkedList<>();

        boolean middleRecordDeleted = false;
        while (middleRecordIndex < originalPage.records.size()) {
            // start from middle record
            // sibling pointers are null because we are on the deepest level
            if (!middleRecordDeleted) {
                middleRecordDeleted = true;

            } else {
                originalPage.records.get(middleRecordIndex).setNodePagePositionInIndex(secondPage.getPositionInIndex());
                secondPage.records.add(originalPage.records.get(middleRecordIndex));
            }
            originalPage.records.remove(middleRecordIndex);
        }


        originalPage.refactorEmptyRecords();
        secondPage.refactorEmptyRecords();

        return middleRecord;
    }

    public Record findRecordByChildPosition(Long childPosition) {
        for (Record record : records) {
            if (
                    record.getLeftChildPositionInIndex().equals(childPosition)
                    || record.getRightChildPositionInIndex().equals(childPosition)
            ) {
                return record;
            }
        }

        return null;
    }

    public Record getLeftSiblingRecord(Record record) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).equals(record)) {
                if (i > 0) {
                    if (records.get(i - 1).getPositionInDataFile() != Consts.EMPTY_VALUE.getValue()){
                        return records.get(i - 1);
                    }
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public Record getRightSiblingRecord(Record record) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).equals(record)) {
                if (i + 1 < records.size()) {
                    if (records.get(i + 1).getPositionInDataFile() != Consts.EMPTY_VALUE.getValue()){
                        return records.get(i + 1);
                    }
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}
