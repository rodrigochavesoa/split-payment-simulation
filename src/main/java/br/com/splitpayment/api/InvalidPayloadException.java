package br.com.splitpayment.api;

public final class InvalidPayloadException extends RuntimeException {
    private final String field;

    public InvalidPayloadException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
