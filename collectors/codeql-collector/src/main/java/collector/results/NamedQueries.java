package collector.results;

public class NamedQueries {

    private String queryName;
    private String queryValue;
    private boolean isNative;

    public NamedQueries(String queryName, String queryValue, boolean isNative) {
        this.queryName = queryName;
        this.queryValue = queryValue;
        this.isNative = isNative;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryValue() {
        return queryValue;
    }

    public void setQueryValue(String queryValue) {
        this.queryValue = queryValue;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }
}
