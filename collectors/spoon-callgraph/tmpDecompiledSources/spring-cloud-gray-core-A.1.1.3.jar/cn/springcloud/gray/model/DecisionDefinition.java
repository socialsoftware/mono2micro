/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DecisionDefinition
implements Serializable {
    private static final long serialVersionUID = 7613293834300650748L;
    private String id;
    private String name;
    private Map<String, String> infos = new HashMap<String, String>();

    public String toString() {
        return "DecisionDefinition(id=" + this.getId() + ", name=" + this.getName() + ", infos=" + this.getInfos() + ")";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInfos(Map<String, String> infos) {
        this.infos = infos;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getInfos() {
        return this.infos;
    }
}

