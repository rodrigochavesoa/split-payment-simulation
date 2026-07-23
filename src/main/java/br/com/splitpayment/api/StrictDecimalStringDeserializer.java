package br.com.splitpayment.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

/** Prevents Jackson from coercing JSON numbers into String fields at the HTTP boundary. */
public final class StrictDecimalStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            throw InvalidFormatException.from(
                    parser,
                    "must be sent as a JSON string to preserve decimal precision",
                    parser.getValueAsString(),
                    String.class
            );
        }
        return parser.getText();
    }
}
