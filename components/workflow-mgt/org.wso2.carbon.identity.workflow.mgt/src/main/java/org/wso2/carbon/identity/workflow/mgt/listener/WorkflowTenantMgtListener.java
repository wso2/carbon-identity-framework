package org.wso2.carbon.identity.workflow.mgt.listener;

import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

/**
 * Workflow Tenant Management Listener.
 */
public class WorkflowTenantMgtListener implements TenantMgtListener {
    private static final int EXEC_ORDER = 12;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {

    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {

    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int i, String s, String s2) throws StratosException {

    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {

    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s2) throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }

    @Override
    public void onPreDelete(int tenantId) throws StratosException {

        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService().removeWorkflows(tenantId);
        } catch (WorkflowException e) {
            throw new StratosException("Server error occurred when removing workflows");
        }
    }

}
