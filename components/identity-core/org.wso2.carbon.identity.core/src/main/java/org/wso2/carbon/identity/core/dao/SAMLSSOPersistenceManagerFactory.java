package org.wso2.carbon.identity.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML_STORAGE_CONFIG;

public class SAMLSSOPersistenceManagerFactory {

    private static final Log LOG = LogFactory.getLog(SAMLSSOPersistenceManagerFactory.class);
    private static final String SAML_STORAGE_TYPE = IdentityUtil.getProperty(SAML_STORAGE_CONFIG);
    private static final String HYBRID = "hybrid";
    private static final String REGISTRY = "registry";

    public SAMLSSOServiceProviderDAO buildSSOServiceProviderManager(){
        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = new JDBCSAMLSSOServiceProviderDAOImpl();
        if (StringUtils.isNotBlank(SAML_STORAGE_TYPE)) {
            switch (SAML_STORAGE_TYPE) {
                case HYBRID:
                    LOG.info("Hybrid SAML storage initialized.");
                    break;
                case REGISTRY:
                    samlSSOServiceProviderDAO = new RegistrySAMLSSOServiceProviderDAOImpl();
                    LOG.info("Registry based SAML storage initialized.");
                    break;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "SAML SSO Service Provider DAO initialized with the type: " + samlSSOServiceProviderDAO.getClass());
        }
        return samlSSOServiceProviderDAO;
    }
}
