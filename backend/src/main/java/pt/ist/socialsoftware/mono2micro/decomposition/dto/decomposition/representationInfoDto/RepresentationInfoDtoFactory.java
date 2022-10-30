package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;

public class RepresentationInfoDtoFactory {
    public static List<RepresentationInfoDto> getRepresentationInfoDtos(List<RepresentationInfo> representationInfos) {
        List<RepresentationInfoDto> representationInfoDtos = new ArrayList<>();
        for (RepresentationInfo representationInfo : representationInfos)
            representationInfoDtos.add(getRepresentationInfoDto(representationInfo));
        return representationInfoDtos;
    }

    public  static RepresentationInfoDto getRepresentationInfoDto(RepresentationInfo representationInfo) {
        switch (representationInfo.getType()) {
            case ACCESSES_INFO:
                return new AccessesInfoDto(representationInfo);
            case REPOSITORY_INFO:
                return new RepositoryInfoDto(representationInfo);
            default:
                throw new RuntimeException("No known representation info type: " + representationInfo.getType());
        }
    }
}
