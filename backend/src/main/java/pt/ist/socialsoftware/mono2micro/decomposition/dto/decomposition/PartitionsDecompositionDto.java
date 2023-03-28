package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.PartitionsDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto.RepresentationInfoDtoFactory;

public class PartitionsDecompositionDto extends DecompositionDto {
    private boolean outdated;
    private boolean expert;

    public PartitionsDecompositionDto(PartitionsDecomposition decomposition) {
        this.setCodebaseName(decomposition.getSimilarity().getStrategy().getCodebase().getName());
        this.setStrategyName(decomposition.getStrategy().getName());
        this.setSimilarityName(decomposition.getSimilarity().getName());
        this.setName(decomposition.getName());
        this.type = decomposition.getType();
        this.setMetrics(decomposition.getMetrics());
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        this.representationInformations = RepresentationInfoDtoFactory.getRepresentationInfoDtos(decomposition.getRepresentationInformations());
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }
}
