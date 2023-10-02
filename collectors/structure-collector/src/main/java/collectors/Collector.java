package collectors;

import spoon.Launcher;

/**
 * This interface defines a code Collector,
 * which controls and stores the source code elements to be retrieved.
 * Subclass {@link AbstractCollector} to define a new Collector.
 */
public interface Collector {

    /**
     * Responsible for starting the collection of source code artifacts by running the Spoon Launcher.
     */
    void collect(Launcher launcher);

    /**
     * Responsible for serializing the collected data.
     */
    void serialize(String outputFileName);
}
