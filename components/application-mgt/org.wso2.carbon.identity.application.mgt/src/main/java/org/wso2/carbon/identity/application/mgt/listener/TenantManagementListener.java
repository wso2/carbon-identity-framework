package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.CacheBackedApplicationDAO;
import org.wso2.carbon.identity.core.AbstractIdentityTenantMgtListener;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.stratos.common.exception.StratosException;

/**
 * Tenant management listener for Service Provider
 */
public class TenantManagementListener extends AbstractIdentityTenantMgtListener {

    private static final Log log = LogFactory.getLog(TenantManagementListener.class);
    private static ApplicationDAOImpl applicationDAO = new ApplicationDAOImpl();
    private static CacheBackedApplicationDAO cacheBackedApplicationDAO = new CacheBackedApplicationDAO(applicationDAO);

    @Override
    public void onTenantDeactivation(int tenantID) throws StratosException {

        try {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
            ApplicationBasicInfo[] applicationBasicInfos = applicationDAO.getAllApplicationBasicInfo(tenantID);
            for (ApplicationBasicInfo applicationBasicInfo: applicationBasicInfos) {
                ServiceProvider sp = new ServiceProvider();
                sp.setApplicationName(applicationBasicInfo.getApplicationName());
                sp.setApplicationID(applicationBasicInfo.getApplicationId());
                sp.setApplicationResourceId(applicationBasicInfo.getApplicationResourceId());
                cacheBackedApplicationDAO.clearApplicationFromCache(sp, tenantDomain);
            }
            if (log.isDebugEnabled()) {
                log.debug("Service Providers of tenant " + tenantDomain + " is cleared from cache.");
            }
        } catch (IdentityApplicationManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
