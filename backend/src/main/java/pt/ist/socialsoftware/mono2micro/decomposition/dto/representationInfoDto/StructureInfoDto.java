package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.StructureInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;

import java.util.ArrayList;
import java.util.Map;

public class StructureInfoDto extends RepresentationInfoDto {
    private Map<String, ArrayList<String>> entitiesContained;
    private Map<String, String>  entitySuperClass;

    public StructureInfoDto(RepresentationInformation representationInformation) {
        super(representationInformation.getType());
        StructureInformation structureInformation = (StructureInformation) representationInformation;
        this.entitiesContained = structureInformation.getEntitiesContained();
        this.entitySuperClass = structureInformation.getEntitySuperClass();
    }

    public Map<String, ArrayList<String>> getEntitiesContained() {return entitiesContained;}

    public Map<String, String> getEntitySuperClass() {return entitySuperClass;}

    public void setEntitiesContained(Map<String, ArrayList<String>> entitiesContained) {this.entitiesContained = entitiesContained;}

    public void setEntitySuperClass(Map<String, String> entitySuperClass) {this.entitySuperClass = entitySuperClass;}
}
