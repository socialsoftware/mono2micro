package pt.ist.socialsoftware.mono2micro.dto.decompositionDto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.ACCESSES_SCIPY;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesSciPyInfoDto.class, name = ACCESSES_SCIPY)
})
public interface InfoDto {
}