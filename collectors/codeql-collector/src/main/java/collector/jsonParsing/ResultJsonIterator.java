package collector.jsonParsing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ResultJsonIterator<T> implements Iterator<T>, Closeable {
    private final JsonParser parser;
    private final ResultMapper<T> mapper;
    private T nextItem;

    public ResultJsonIterator(File jsonFile, ResultMapper<T> mapper) throws IOException {
        this.mapper = mapper;
        ObjectMapper objectMapper = new ObjectMapper();
        this.parser = objectMapper.getFactory().createParser(jsonFile);
        this.parser.setCodec(objectMapper);
        advanceToTuplesArray();
        advance();
    }

    private void advanceToTuplesArray() throws IOException {

        while (!parser.isClosed()) {
            JsonToken token = parser.nextToken();
            if (token == null) {
                break;
            }

            if (JsonToken.FIELD_NAME.equals(token) && "#select".equals(parser.getCurrentName())) {
                parser.nextToken(); // Should be START_OBJECT
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    if ("tuples".equals(parser.getCurrentName())) {
                        JsonToken next = parser.nextToken(); // Should be START_ARRAY
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            return;
                        }
                    } else {
                        parser.skipChildren();
                    }
                }
            }
        }

        throw new IOException("Could not find '#select.tuples' array in the JSON structure");
    }

    private void advance() {
        nextItem = null;
        try {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                // Expecting START_ARRAY (start of one tuple)
                if (parser.currentToken() != JsonToken.START_ARRAY) {
                    parser.skipChildren();  // defensive: skip malformed tuple
                    continue;
                }

                List<JsonNode> fields = new ArrayList<>();

                // Inner loop: read all fields of one tuple
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    JsonToken token = parser.currentToken();

                    if (token == JsonToken.VALUE_STRING) {
                        fields.add(new TextNode(parser.getValueAsString()));
                    } else if (token == JsonToken.START_OBJECT) {
                        fields.add(parser.readValueAs(ObjectNode.class));
                    } else {
                        parser.skipChildren(); // skip unexpected structures
                    }
                }

                // Map and return if not null
                T mapped = mapper.map(fields);
                if (mapped != null) {
                    nextItem = mapped;
                    return;
                }
            }

            parser.close();  // end of tuples array

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextItem != null;
    }

    @Override
    public T next() {
        if (nextItem == null) {
            throw new NoSuchElementException();
        }
        T result = nextItem;
        advance();
        return result;
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }
}
