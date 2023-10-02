package gui;

import java.io.File;

public class Constants {

    // Frameworks
    public static final int SPRING_DATA_JPA = 0;
    public static final int FENIX_FRAMEWORK = 1;

    // Data collection
    public static final int DOMAIN_ENTITY_DATA_COLLECTION = 1;

    public static final String OUTPUT_PATH =
            System.getProperty("user.dir") + File.separator + "data" + File.separator + "collection" + File.separator;

}
