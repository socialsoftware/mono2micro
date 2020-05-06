/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.model;

import cn.springcloud.gray.model.DecisionDefinition;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PolicyDefinition
implements Serializable {
    private static final long serialVersionUID = -1L;
    private String policyId;
    private String alias;
    private List<DecisionDefinition> list = new CopyOnWriteArrayList<DecisionDefinition>();

    public String toString() {
        return "PolicyDefinition(policyId=" + this.getPolicyId() + ", alias=" + this.getAlias() + ", list=" + this.getList() + ")";
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setList(List<DecisionDefinition> list) {
        this.list = list;
    }

    public String getPolicyId() {
        return this.policyId;
    }

    public String getAlias() {
        return this.alias;
    }

    public List<DecisionDefinition> getList() {
        return this.list;
    }
}

