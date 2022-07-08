package pt.ist.socialsoftware.mono2micro.codebase.dto;

import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.source.dto.SourceDto;
import pt.ist.socialsoftware.mono2micro.source.dto.SourceDtoFactory;

import java.util.List;

public class CodebaseDto {
    private String name;

    private List<SourceDto> sources;

    private boolean isEmpty;

    public CodebaseDto(Codebase codebase) {
        this.name = codebase.getName();
        this.sources = SourceDtoFactory.getFactory().getSourceDtos(codebase.getSources());
        this.isEmpty = codebase.isEmpty();
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

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        this.isEmpty = empty;
    }
}
