// Program is limited by Long.
public enum Consts {
    DEGREE(2),
    RECORDS_NUMBER_IN_NODE_PAGE(2 * DEGREE.value),

    FILE_NULL(-1),
    EMPTY_VALUE(-2),
    MIN_KEY(-99),
    MAX_KEY(100),


    // = MAX_KEY + 1 (zero) + |MIN_KEY|
    MAX_RECORDS_NUMBER(200),
    // also there won't be more than MAX_RECORDS_NUMBER positions in index (assuming that I use deleted space)
    // even if every record was on separate node page


    // page in file structure: | ancestor position | p_0 | x_1 | a_1 | p_1 | ... | x_2d | a_2d | p_2d |
    // ancestor pos., p_i, x_i, a_i  <= MAX_RECORDS_NUMBER
    // fields have fixed number of chars because there is MAX_RECORDS_NUMBER (no duplicates and deleted space used)
    CHILD_POINTERS_NUMBER_IN_NODE_PAGE(RECORDS_NUMBER_IN_NODE_PAGE.value + 1),
    FIELDS_NUMBER_ON_INDEX_PAGE(2 * RECORDS_NUMBER_IN_NODE_PAGE.value // record = (x, a) => '2 *'
            + CHILD_POINTERS_NUMBER_IN_NODE_PAGE.value // pointers
            + 1), // ancestor position
    INDEX_PAGE_FIELD_CHARS_NUMBER(3), // MAX_RECORDS_NUMBER will fit in 3 chars
    NODE_PAGE_BYTES_SIZE(INDEX_PAGE_FIELD_CHARS_NUMBER.value * FIELDS_NUMBER_ON_INDEX_PAGE.value),

    MIN_PARAMETER(-5),
    MAX_PARAMETER(5),
    DATA_FILE_PARAMETER_CHARS_NUMBER(5), // max digit number -> a4 * x^4 -> -5 * (-5^4) = -3125 => ~5 digits
    PARAMETERS_NUMBER(6), // 44. Rekordy pliku: parametry a0,a1,a2,a3,a4,x.

    PARAMETER_SIZE_IN_BYTES(DATA_FILE_PARAMETER_CHARS_NUMBER.value),
    DATA_FILE_RECORD_SIZE_IN_BYTES(PARAMETERS_NUMBER.value * PARAMETER_SIZE_IN_BYTES.value),
    DATA_FILE_RECORDS_NUMBER_IN_BLOCK(4),
    DATA_FILE_BLOCK_SIZE_IN_BYTES(DATA_FILE_RECORDS_NUMBER_IN_BLOCK.value * DATA_FILE_RECORD_SIZE_IN_BYTES.value);

    private final int value;

    Consts(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
