/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationTemplateDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.OAuthApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.SAMLApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationTemplateDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.IdentityProviderDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.OAuthApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.SAMLApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.CacheBackedApplicationDAO;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This instance holds all the system configurations
 */
public class ApplicationMgtSystemConfig {

    private static final Log log = LogFactory.getLog(ApplicationMgtSystemConfig.class);
    // Configuration elements in the application-authentication.xml
    private static final String CONFIG_ELEMENT_SP_MGT = "ServiceProvidersManagement";
    private static final String CONFIG_APPLICATION_DAO = "ApplicationDAO";
    private static final String CONFIG_OAUTH_OIDC_DAO = "OAuthOIDCClientDAO";
    private static final String CONFIG_SAML_DAO = "SAMLClientDAO";
    private static final String CONFIG_SYSTEM_IDP_DAO = "SystemIDPDAO";
    private static final String CONFIG_APPLICATION_TEMPLATE_DAO = "ApplicationTemplateDAO";
    private static final String CONFIG_CLAIM_DIALECT = "ClaimDialect";
    private static volatile ApplicationMgtSystemConfig instance = null;
    // configured String values
    private String appDAOClassName = null;
    private String oauthDAOClassName = null;
    private String samlDAOClassName = null;
    private String systemIDPDAPClassName = null;
    private String appTemplateDAOClassName = null;
    private String claimDialect = null;


    private ApplicationMgtSystemConfig() {
        buildSystemConfiguration();
    }

    /**
     * Returns the Singleton of <code>ApplicationMgtSystemConfig</code>
     *
     * @return
     */
    public static ApplicationMgtSystemConfig getInstance() {
        CarbonUtils.checkSecurity();
        if (instance == null) {
            synchronized (ApplicationMgtSystemConfig.class) {
                if (instance == null) {
                    instance = new ApplicationMgtSystemConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Start building the system config
     */
    private void buildSystemConfiguration() {

        OMElement spConfigElem = IdentityConfigParser.getInstance().getConfigElement(CONFIG_ELEMENT_SP_MGT);

        if (spConfigElem == null) {
            if(log.isDebugEnabled()){
                log.debug("No ServiceProvidersManagement configuration found. System Starts with default configuration");
            }
        } else {
            // application DAO class
            OMElement appDAOConfigElem = spConfigElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(CONFIG_APPLICATION_DAO));
            if (appDAOConfigElem != null) {
                appDAOClassName = appDAOConfigElem.getText().trim();
            }

            // OAuth and OpenID Connect DAO class
            OMElement oauthOidcDAOConfigElem = spConfigElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(CONFIG_OAUTH_OIDC_DAO));
            if (oauthOidcDAOConfigElem != null) {
                oauthDAOClassName = oauthOidcDAOConfigElem.getText().trim();
            }

            // SAML DAO class
            OMElement samlDAOConfigElem = spConfigElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(CONFIG_SAML_DAO));
            if (samlDAOConfigElem != null) {
                samlDAOClassName = samlDAOConfigElem.getText().trim();
            }

            // IDP DAO class
            OMElement idpDAOConfigElem = spConfigElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(CONFIG_SYSTEM_IDP_DAO));
            if (idpDAOConfigElem != null) {
                systemIDPDAPClassName = idpDAOConfigElem.getText().trim();
            }

            // Application Template DAO class
            OMElement appTemplateDAOConfigElem = spConfigElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(CONFIG_APPLICATION_TEMPLATE_DAO));
            if (appTemplateDAOConfigElem != null) {
                appTemplateDAOClassName = appTemplateDAOConfigElem.getText().trim();
            }

            OMElement claimDAOConfigElem = spConfigElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(CONFIG_CLAIM_DIALECT));
            if (claimDAOConfigElem != null) {
                claimDialect = claimDAOConfigElem.getText().trim();
            }

        }
    }

    /**
     * Return an instance of the ApplicationDAO
     *
     * @return
     */
    public ApplicationDAO getApplicationDAO() {

        ApplicationDAO applicationDAO = null;

        if (appDAOClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(appDAOClassName);
                applicationDAO = (ApplicationDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the ApplicationDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the ApplicationDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the ApplicationDAO ", e);
            }

        } else {
            applicationDAO = new ApplicationDAOImpl();
        }
        return new CacheBackedApplicationDAO(applicationDAO);
    }

    /**
     * Return an instance of the OAuthOIDCClientDAO
     *
     * @return
     */
    public OAuthApplicationDAO getOAuthOIDCClientDAO() {

        OAuthApplicationDAO oauthOidcDAO = null;

        if (oauthDAOClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(oauthDAOClassName);
                oauthOidcDAO = (OAuthApplicationDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the OAuthOIDCClientDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the OAuthOIDCClientDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the OAuthOIDCClientDAO ", e);
            }

        } else {
            oauthOidcDAO = new OAuthApplicationDAOImpl();
        }

        return oauthOidcDAO;
    }

    /**
     * Return an instance of the SAMLClientDAO
     *
     * @return
     */
    public SAMLApplicationDAO getSAMLClientDAO() {

        SAMLApplicationDAO samlDAO = null;

        if (samlDAOClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(samlDAOClassName);
                samlDAO = (SAMLApplicationDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            }

        } else {
            samlDAO = new SAMLApplicationDAOImpl();
        }

        return samlDAO;
    }


    /**
     * Return an instance of the SystemIDPDAO
     *
     * @return
     */
    public IdentityProviderDAO getIdentityProviderDAO() {

        IdentityProviderDAO idpDAO = null;

        if (systemIDPDAPClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(systemIDPDAPClassName);
                idpDAO = (IdentityProviderDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            }

        } else {
            idpDAO = new IdentityProviderDAOImpl();
        }

        return idpDAO;
    }

    /**
     * Return an instance of the ApplicationDAO
     *
     * @return
     */
    public ApplicationTemplateDAO getApplicationTemplateDAO() {
        ApplicationTemplateDAO applicationTemplateDAO = null;
        if (appTemplateDAOClassName != null) {
            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(appTemplateDAOClassName);
                applicationTemplateDAO = (ApplicationTemplateDAO) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the ApplicationTemplateDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the ApplicationTemplateDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the ApplicationTemplateDAO ", e);
            }
        } else {
            applicationTemplateDAO = new ApplicationTemplateDAOImpl();
        }
        return applicationTemplateDAO;
    }

    /**
     * Returns the claim dialect for claim mappings
     *
     * @return
     */
    public String getClaimDialect() {
        if (claimDialect != null) {
            return claimDialect;
        }
        return "http://wso2.org/claims";
    }

}
