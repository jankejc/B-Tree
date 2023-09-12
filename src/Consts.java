public enum Consts {
    DEGREE(2),

    MIN_KEY(-99),
    MAX_KEY(100),
    FIXED_CHARS_NUMBER_INDEX(3), // keys [-99;100] (3 chars) -> max. 200 nodes (3 chars);
    NODE_PAGE_BYTES_SIZE(FIXED_CHARS_NUMBER_INDEX.value * 2 * DEGREE.value), // How much chars when put in file.

    MIN_PARAMETER(-5),
    MAX_PARAMETER(5),
    FIXED_CHARS_NUMBER_DATA(5), // max digit number -> a4 * x^4 -> -5 * (-5^4) = -3125 => ~5 digits
    PARAMETER_NUMBERS(6); // 44. Rekordy pliku: parametry a0,a1,a2,a3,a4,x.

    private final int value;

    Consts(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
