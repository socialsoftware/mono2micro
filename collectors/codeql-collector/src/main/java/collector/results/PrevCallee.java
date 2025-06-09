package collector.results;

public class PrevCallee {

    private String prevType;
    private String location;

    public PrevCallee(String prevType, String location) {
        this.prevType = prevType;
        this.location = location;
    }

    public String getPrevType() {
        return prevType;
    }

    public void setPrevType(String prevType) {
        this.prevType = prevType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
