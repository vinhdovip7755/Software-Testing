package com.bookstore.api.contract;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/** Prevent Jackson's default float-to-int coercion for quantities sold. */
public class BillQuantityDeserializer extends StdDeserializer<Integer> {
    public BillQuantityDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return (Integer) context.handleUnexpectedToken(Integer.class, parser);
        }
        return parser.getIntValue();
    }

    @Override
    public Integer getNullValue(DeserializationContext context) {
        // The existing bill validation rejects zero, including missing/null quantities.
        return 0;
    }
}
