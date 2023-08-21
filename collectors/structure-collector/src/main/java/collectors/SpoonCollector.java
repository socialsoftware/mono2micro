package collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.DomainEntity;
import gui.Constants;
import processors.fenixframework.FenixFrameworkDomainEntityProcessor;
import processors.springboot.SpringDataJpaDomainEntityProcessor;
import spoon.Launcher;
import spoon.processing.Processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Enhancement - Decouple cached data from collector class ("domainEntities")
public class SpoonCollector implements Collector {

    @JsonIgnore
    private final Launcher spoonLauncher;

    @JsonIgnore
    private String projectName;
    @JsonProperty(value = "entities")
    private List<DomainEntity> domainEntities;

    public SpoonCollector(String projectName, String sourcesPath, int projectFramework) {
        this.projectName = projectName;
        this.domainEntities = new ArrayList<>();

        this.spoonLauncher = new Launcher();
        setLauncherSourcesPath(sourcesPath);
        setLauncherProcessorStrategy(projectFramework);
    }

    private void setLauncherSourcesPath(String sourcesPath) {
        this.spoonLauncher.addInputResource(sourcesPath.trim());
    }

    private void setLauncherProcessorStrategy(int projectFramework) {
        switch (projectFramework) {
            case Constants.SPRING_DATA_JPA:
                addProcessorToLauncher(new SpringDataJpaDomainEntityProcessor(this));
                break;
            case Constants.FENIX_FRAMEWORK:
                addProcessorToLauncher(new FenixFrameworkDomainEntityProcessor(this));
                break;
            default:
                System.err.println("Project framework code not recognized: " + projectFramework);
                System.exit(1);
        }
    }

    private void addProcessorToLauncher(Processor<?> spoonProcessor) {
        this.spoonLauncher.addProcessor(spoonProcessor);
    }

    @Override
    public Collector collect() {
        this.spoonLauncher.run();
        return this;
    }

    // TODO: Make serialization strategy depend on input
    @Override
    public void serialize() {
        File filePath = new File(Constants.OUTPUT_PATH);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        filePath.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(Constants.OUTPUT_PATH + this.projectName + "_m2m_structure.json")) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DomainEntity> getDomainEntities() {
        return Collections.unmodifiableList(domainEntities);
    }

    public void addDomainEntity(DomainEntity domainEntity) {
        this.domainEntities.add(domainEntity);
    }

    @JsonIgnore
    public int getDomainEntitiesSize() {
        return domainEntities.size();
    }
}