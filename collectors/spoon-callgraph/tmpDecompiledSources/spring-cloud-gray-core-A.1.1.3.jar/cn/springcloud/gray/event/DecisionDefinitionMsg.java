/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.event;

import cn.springcloud.gray.model.DecisionDefinition;
import java.io.Serializable;

public class DecisionDefinitionMsg
extends DecisionDefinition
implements Serializable {
    private static final long serialVersionUID = 7613293834300650748L;
    private String policyId;

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyId() {
        return this.policyId;
    }
}

