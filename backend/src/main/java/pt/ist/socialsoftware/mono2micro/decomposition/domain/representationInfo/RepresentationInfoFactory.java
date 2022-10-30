package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;

public class RepresentationInfoFactory {
    public static List<RepresentationInfo> getRepresentationInfosFromType(List<String> representationInfosTypes) {
        List<RepresentationInfo> representationInfos = new ArrayList<>();
        for (String rep : representationInfosTypes)
            representationInfos.add(getRepresentationInfoFromType(rep));
        return representationInfos;
    }

    public static RepresentationInfo getRepresentationInfoFromType(String type) {
        switch (type) {
            case ACCESSES_INFO:
                return new AccessesInfo();
            case REPOSITORY_INFO:
                return new RepositoryInfo();
            default:
                throw new RuntimeException("Unknown Representation Info type: " + type);
        }
    }
}
