package collectors;

import gui.Constants;
import processors.fenixframework.FenixFrameworkDomainEntityProcessor;
import processors.springboot.SpringDataJpaDomainEntityProcessor;
import spoon.Launcher;
import spoon.processing.Processor;

/**
 * This class is used to build and launch a collector based on user input parameters.
 */
public class CollectorLauncher {

    private final Launcher spoonLauncher = new Launcher();
    private String outputFileName;
    private int projectFramework;
    private int dataCollection;

    public CollectorLauncher(String sourcesPath, String outputFileName, int projectFramework, int dataCollection) {
        setLauncherSourcesPath(sourcesPath);
        setOutputFileName(outputFileName);
        setProjectFramework(projectFramework);
        setDataCollection(dataCollection);
    }

    private void setLauncherSourcesPath(String sourcesPath) {
        this.spoonLauncher.addInputResource(sourcesPath.trim());
    }

    private void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    private void setProjectFramework(int projectFramework) {
        this.projectFramework = projectFramework;
    }

    private void setDataCollection(int dataCollection) {
        this.dataCollection = dataCollection;
    }

    public void launch() {
        Collector collector = buildCollector();
        if (collector != null) {
            collector.collect(spoonLauncher);
            collector.serialize(outputFileName);
        }
    }

    protected Collector buildCollector() {
        if (dataCollection == Constants.DOMAIN_ENTITY_DATA_COLLECTION) {
            return createDomainEntityCollector();
        }

        System.err.println("Collection strategy code number not recognized: " + dataCollection);
        System.exit(1);
        return null;
    }

    protected Collector createDomainEntityCollector() {
        DomainEntityCollector collector = new DomainEntityCollector();
        switch (projectFramework) {
            case Constants.SPRING_DATA_JPA:
                addProcessorToLauncher(new SpringDataJpaDomainEntityProcessor(collector));
                break;
            case Constants.FENIX_FRAMEWORK:
                addProcessorToLauncher(new FenixFrameworkDomainEntityProcessor(collector));
                break;
            default:
                System.err.println("Project framework code number not recognized: " + projectFramework);
                System.exit(1);
        }

        return collector;
    }

    protected void addProcessorToLauncher(Processor<?> processor) {
        this.spoonLauncher.addProcessor(processor);
    }
}
