package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static Integer lineno() { return new Throwable().getStackTrace()[1].getLineNumber(); }

    public static void print(String message, Integer lineNumber) { System.out.println("[" + lineNumber + "] " + message); }

    public static List<String> getJsonFileKeys(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonfactory = mapper.getFactory();

        JsonParser jsonParser = jsonfactory.createParser(is);
        JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT

        if (jsonToken != JsonToken.START_OBJECT) {
            System.err.println("Json must start with a left curly brace");
            System.exit(-1);
        }

        List<String> keys = new ArrayList<>();

        while (true) {
            if (jsonToken == JsonToken.END_OBJECT)
                break;

            // FIXME Wrong! Starting with an array should not be an assumption.
            if (jsonToken == JsonToken.START_ARRAY && jsonParser.getCurrentName() != null) {
                String controllerName = jsonParser.getCurrentName();
                System.out.println("Controller name: " + controllerName);
                keys.add(controllerName);
            }

            jsonToken = jsonParser.nextValue();
        }

        return keys;
    }
}
