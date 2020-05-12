package util;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final int LAUNCHER = 0;
    public static final int MAVEN_LAUNCHER = 1;
    public static final int JAR_LAUNCHER = 2;

    public static final int LOCAL = 0;
    public static final int GITHUB = 1;

    public static final int FENIX_FRAMEWORK = 0;
    public static final int SPRING_DATA_JPA = 1;

    public static final String COLLECTION_SAVE_PATH =
            System.getProperty("user.dir") + File.separator + "data" + File.separator + "collection" + File.separator;
    public static final String DECOMPILED_SOURCES_PATH =
            System.getProperty("user.dir") + File.separator + "tmpDecompiledSources" + File.separator;
    public static final String TEST_DATA_PATH =
            System.getProperty("user.dir") + File.separator + "testData.txt";


    public static final Map<Integer, String> launcherIntToName;
    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "Launcher");
        map.put(1, "Maven Launcher");
        map.put(2, "Jar Launcher");
        launcherIntToName = Collections.unmodifiableMap(map);
    }
}
