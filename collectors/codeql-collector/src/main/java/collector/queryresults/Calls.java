package collector.queryresults;

public class Calls {

    private String callerId;
    private String calleeId;
    private String callLocation;

    public Calls() {
    }

    public Calls(String callerId, String calleeId, String callLocation) {
        this.callerId = callerId;
        this.calleeId = calleeId;
        this.callLocation = callLocation;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getCalleeId() {
        return calleeId;
    }

    public void setCalleeId(String calleeId) {
        this.calleeId = calleeId;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }
}
