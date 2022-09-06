package pt.ist.socialsoftware.mono2micro.codebase.dto;

import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.source.dto.SourceDto;
import pt.ist.socialsoftware.mono2micro.source.dto.SourceDtoFactory;

import java.util.List;

public class CodebaseDto {
    private String name;

    private List<SourceDto> sources;

    public CodebaseDto(Codebase codebase) {
        this.name = codebase.getName();
        this.sources = SourceDtoFactory.getFactory().getSourceDtos(codebase.getSources());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SourceDto> getSources() {
        return sources;
    }

    public void setSources(List<SourceDto> sources) {
        this.sources = sources;
    }
}
