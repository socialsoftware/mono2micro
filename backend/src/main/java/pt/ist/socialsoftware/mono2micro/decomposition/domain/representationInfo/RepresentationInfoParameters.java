package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

public enum RepresentationInfoParameters {
    PROFILE_PARAMETER("PROFILE_PARAMETER"),
    TRACES_MAX_LIMIT_PARAMETER("TRACES_MAX_LIMIT_PARAMETER"),
    TRACE_TYPE_PARAMETER("TRACE_TYPE_PARAMETER"),
    DEPTH_PARAMETER("DEPTH_PARAMETER");

    private final String text;

    RepresentationInfoParameters(final String text) { this.text = text; }

    @Override
    public String toString() { return text; }
}