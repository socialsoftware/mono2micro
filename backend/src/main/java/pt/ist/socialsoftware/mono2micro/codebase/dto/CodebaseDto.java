package pt.ist.socialsoftware.mono2micro.codebase.dto;

import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDto;
import pt.ist.socialsoftware.mono2micro.representation.dto.RepresentationDtoFactory;

import java.util.List;

public class CodebaseDto {
    private String name;

    private List<RepresentationDto> representations;

    public CodebaseDto(Codebase codebase) {
        this.name = codebase.getName();
        this.representations = RepresentationDtoFactory.getFactory().getRepresentationDtos(codebase.getRepresentations());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RepresentationDto> getRepresentations() {
        return representations;
    }

    public void setRepresentations(List<RepresentationDto> representations) {
        this.representations = representations;
    }
}
