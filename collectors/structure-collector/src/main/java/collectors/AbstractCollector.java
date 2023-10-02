package collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import gui.Constants;
import spoon.Launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Base implementation for any {@link Collector}.
 */
public abstract class AbstractCollector implements Collector {

    @Override
    public void collect(Launcher launcher) {
        launcher.run();
    }

    @Override
    public void serialize(String outputFileName) {
        File filePath = new File(Constants.OUTPUT_PATH);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        filePath.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(Constants.OUTPUT_PATH + outputFileName + ".json")) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
