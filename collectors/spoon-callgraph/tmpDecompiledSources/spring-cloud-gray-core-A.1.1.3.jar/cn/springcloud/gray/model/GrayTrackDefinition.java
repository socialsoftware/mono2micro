/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.model;

public class GrayTrackDefinition {
    private String name;
    private String value;

    public String toString() {
        return "GrayTrackDefinition(name=" + this.getName() + ", value=" + this.getValue() + ")";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}

