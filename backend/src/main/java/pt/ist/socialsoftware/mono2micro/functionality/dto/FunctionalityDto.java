package pt.ist.socialsoftware.mono2micro.functionality.dto;

import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityType;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionalityDto {
    private String name;
    private FunctionalityType type;
    private Map<String, Object> metrics;
    private Map<Short, Byte> entities;
    private List<FunctionalityRedesign> functionalityRedesigns;
    private String functionalityRedesignNameUsedForMetrics;
    private Map<String, Set<Short>> entitiesPerCluster;

    public FunctionalityDto(Functionality functionality, List<FunctionalityRedesign> functionalityRedesigns) {
        this.name = functionality.getName();
        this.type = functionality.getType();
        this.metrics = functionality.getMetrics();
        this.entities = functionality.getEntities();
        this.functionalityRedesignNameUsedForMetrics = functionality.getFunctionalityRedesignNameUsedForMetrics();
        this.entitiesPerCluster = functionality.getEntitiesPerCluster();
        this.functionalityRedesigns = functionalityRedesigns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionalityType getType() {
        return type;
    }

    public void setType(FunctionalityType type) {
        this.type = type;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public Map<Short, Byte> getEntities() {
        return entities;
    }

    public void setEntities(Map<Short, Byte> entities) {
        this.entities = entities;
    }

    public List<FunctionalityRedesign> getFunctionalityRedesigns() {
        return functionalityRedesigns;
    }

    public void setFunctionalityRedesigns(List<FunctionalityRedesign> functionalityRedesigns) {
        this.functionalityRedesigns = functionalityRedesigns;
    }

    public String getFunctionalityRedesignNameUsedForMetrics() {
        return functionalityRedesignNameUsedForMetrics;
    }

    public void setFunctionalityRedesignNameUsedForMetrics(String functionalityRedesignNameUsedForMetrics) {
        this.functionalityRedesignNameUsedForMetrics = functionalityRedesignNameUsedForMetrics;
    }

    public Map<String, Set<Short>> getEntitiesPerCluster() {
        return entitiesPerCluster;
    }

    public void setEntitiesPerCluster(Map<String, Set<Short>> entitiesPerCluster) {
        this.entitiesPerCluster = entitiesPerCluster;
    }
}