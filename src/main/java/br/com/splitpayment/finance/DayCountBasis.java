package br.com.splitpayment.finance;

public enum DayCountBasis {
    THIRTY(30),
    THREE_SIXTY(360),
    THREE_SIXTY_FIVE(365);

    private final int days;

    DayCountBasis(int days) {
        this.days = days;
    }

    public int days() {
        return days;
    }
}
