package collector.queryresults;

public class CallQualifier {

    private String callLocation;
    private String entityLocation;

    public CallQualifier() {
    }

    public CallQualifier(String callLocation, String entityLocation) {
        this.callLocation = callLocation;
        this.entityLocation = entityLocation;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public String getEntityLocation() {
        return entityLocation;
    }

    public void setEntityLocation(String entityLocation) {
        this.entityLocation = entityLocation;
    }
}
