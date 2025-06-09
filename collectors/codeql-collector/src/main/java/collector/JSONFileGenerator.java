package collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

import static collector.Constants.OUTPUT_PATH;

public class JSONFileGenerator {

    public void outputToJson(ObjectMapper mapper, String filePath, ObjectNode node) {
        try {
            // Get output dir and check if it exists
            File outputDir = new File(OUTPUT_PATH);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            // Output to file
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, filePath), node);
        } catch (IOException e) {
            System.err.println("Error creating json output for file: " + filePath);
        }
    }

}
