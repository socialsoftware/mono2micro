package util;

public class Query {
    private String name;
    private String value;
    private boolean isNative;

    public Query(String name, String value, boolean isNative) {
        this.name = name;
        this.value = value;
        this.isNative = isNative;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }
}
