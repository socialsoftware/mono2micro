package pt.ist.socialsoftware.mono2micro.functionality.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldType {
    private String name;
    private List<FieldType> parameters; // for List type

    public FieldType() {}

    @JsonCreator
    public FieldType(
        @JsonProperty("name") String name
    ) {
        this.name = name;
    }


    @JsonCreator
    public FieldType(
        @JsonProperty("name") String name,
        @JsonProperty("parameters") List<FieldType> parameters
    ) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public List<FieldType> getParameters() { return this.parameters; }
    public void setParameters(List<FieldType> parameters) { this.parameters = parameters; }
}
