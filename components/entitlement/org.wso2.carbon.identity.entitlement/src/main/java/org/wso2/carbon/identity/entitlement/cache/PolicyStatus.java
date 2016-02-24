package org.wso2.carbon.identity.entitlement.cache;

import java.io.Serializable;

/**
 * Created by harsha on 1/25/15.
 */
public class PolicyStatus implements Serializable {

    private static final long serialVersionUID = -5173389109938987102L;

    private String policyId = null;
    private int statusCount = 0;
    private String policyAction;

    public PolicyStatus() {

    }

    public PolicyStatus(String policyId) {
        this.policyId = policyId;
    }

    public PolicyStatus(String policyId, int statusCount, String policyAction) {
        this.policyId = policyId;
        this.statusCount = statusCount;
        this.policyAction = policyAction;
    }

    public PolicyStatus(int statusCount, String policyAction) {
        this.statusCount = statusCount;
        this.policyAction = policyAction;
    }

    public int getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(int statusCount) {
        this.statusCount = statusCount;
    }

    public String getPolicyAction() {
        return policyAction;
    }

    public void setPolicyAction(String policyAction) {
        this.policyAction = policyAction;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }


}
