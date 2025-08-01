package collector.jsonParsing;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@FunctionalInterface
public interface ResultMapper<T> {
    T map(List<JsonNode> tupleFields);
}
