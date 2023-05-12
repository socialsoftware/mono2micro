import com.fasterxml.jackson.databind.ObjectMapper;
import processors.ASTCache;
import processors.SpringDomainEntityProcessor;
import spoon.Launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static final String FILE_PATH =
            System.getProperty("user.dir") + File.separator + "data" + File.separator + "collection" + File.separator;

    public static final String FILE_NAME = "quizzes-tutor-structure.json";

    public static void demo() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("./data/test/quizzes-tutor-simple/src");

        ASTCache astCache = new ASTCache();
        launcher.addProcessor(new SpringDomainEntityProcessor(astCache));
        launcher.run();

        File filePath = new File(FILE_PATH);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(FILE_PATH + "test.json")) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, astCache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        demo();
    }
}
