package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationContextOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to optimize the Authentication Context before storing it and again loaded it with objects.
 */
public class AuthenticationContextLoader {

    private static volatile AuthenticationContextLoader instance;

    private AuthenticationContextLoader() { }

    public static AuthenticationContextLoader getInstance() {

        if (instance == null) {
            synchronized (AuthenticationContextLoader.class) {
                if (instance == null) {
                    instance = new AuthenticationContextLoader();
                }
            }
        }
        return instance;
    }

    public void optimizeAuthenticationContext(AuthenticationContext context)
            throws AuthenticationContextOptimizationException {

        optimizeExternalIdP(context);
        optimizeAuthenticatorConfig(context);
        optimizeApplicationConfig(context);
    }

    public void loadAuthenticationContext(AuthenticationContext context) throws
            AuthenticationContextOptimizationException {

        loadExternalIdP(context);
        loadAuthenticatorConfig(context);
        loadApplicationConfig(context);
    }

    private void optimizeExternalIdP(AuthenticationContext context) {

        if (context.getExternalIdP() != null) {
            context.setExternalIdPResourceId(context.getExternalIdP().getIdentityProvider().getResourceId());
        }
        context.setExternalIdP(null);
    }

    private void loadExternalIdP(AuthenticationContext context) throws AuthenticationContextOptimizationException {

        if (context.getExternalIdP() == null && context.getExternalIdPResourceId() != null) {
            IdentityProvider idp = getIdPsByResourceID(context.getExternalIdPResourceId(), context.getTenantDomain());
            context.setExternalIdP(new ExternalIdPConfig(idp));
            context.setExternalIdPResourceId(null);
        }
    }

    private void optimizeAuthenticatorConfig(AuthenticationContext context)
            throws AuthenticationContextOptimizationException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        if (sequenceConfig != null) {
            for (Map.Entry<Integer, StepConfig> mapEntry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = mapEntry.getValue();
                if (stepConfig.getAuthenticatedAutenticator() != null) {
                    stepConfig.setAuthenticatedAuthenticatorName(stepConfig.getAuthenticatedAutenticator().getName());
                    stepConfig.setAuthenticatedAutenticator(null);
                }
                List<AuthenticatorConfig> authenticatorList = stepConfig.getAuthenticatorList();
                for (AuthenticatorConfig authenticatorConfig : authenticatorList) {
                    authenticatorConfig.setIdPResourceIds(new ArrayList<>());
                    authenticatorConfig.setApplicationAuthenticator(null);
                    for (Map.Entry<String, IdentityProvider> entry : authenticatorConfig.getIdps().entrySet()) {
                        String idpName = entry.getKey();
                        IdentityProvider idp = entry.getValue();
                        if (idp.getResourceId() == null) {
                            authenticatorConfig.addResourceId(
                                    getIdPsByIdPName(idpName, context.getTenantDomain()).getResourceId());
                        } else {
                            authenticatorConfig.addResourceId(idp.getResourceId());
                        }
                    }
                    authenticatorConfig.setIdPs(null);
                    authenticatorConfig.setIdPNames(null);
                }
            }
        }
    }

    private void loadAuthenticatorConfig(AuthenticationContext context)
            throws AuthenticationContextOptimizationException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        if (sequenceConfig != null) {
            for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = entry.getValue();
                for (AuthenticatorConfig authenticatorConfig : stepConfig.getAuthenticatorList()) {
                    if (authenticatorConfig.getApplicationAuthenticator() == null) {
                        authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.
                                getAppAuthenticatorByName(authenticatorConfig.getName()));
                    }
                    if (authenticatorConfig.getIdps() == null && authenticatorConfig.getIdpNames() == null) {
                        authenticatorConfig.setIdPs(new HashMap<>());
                        authenticatorConfig.setIdPNames(new ArrayList<>());
                        for (String resourceId : authenticatorConfig.getIdPResourceIds()) {
                            IdentityProvider idp = getIdPsByResourceID(resourceId, context.getTenantDomain());
                            authenticatorConfig.addIdPNames(idp.getIdentityProviderName());
                            authenticatorConfig.addIdPs(idp.getIdentityProviderName(), idp);
                        }
                    }
                }
                if (stepConfig.getAuthenticatedAutenticator() == null) {
                    stepConfig.getAuthenticatorList().forEach(authConfig -> {
                        if (authConfig.getName().equals(stepConfig.getAuthenticatedAuthenticatorName())) {
                            stepConfig.setAuthenticatedAutenticator(authConfig);
                        }
                    });
                }
            }
        }
    }

    private void optimizeApplicationConfig(AuthenticationContext context) throws
            AuthenticationContextOptimizationException {

        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        if (applicationConfig != null) {
            OptimizedApplicationConfig optApplicationConfig = new OptimizedApplicationConfig(applicationConfig,
                    context.getTenantDomain());
            context.getSequenceConfig().setOptApplicationConfig(optApplicationConfig);
            context.getSequenceConfig().setApplicationConfig(null);
        }
    }

    private void loadApplicationConfig(AuthenticationContext context)
            throws AuthenticationContextOptimizationException {

        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        OptimizedApplicationConfig optApplicationConfig = context.getSequenceConfig().getOptApplicationConfig();
        if (applicationConfig == null && optApplicationConfig != null) {
            ServiceProvider serviceProvider = reconstructServiceProvider(optApplicationConfig,
                    context.getTenantDomain());
            if (serviceProvider == null) {
                throw new AuthenticationContextOptimizationException(
                        String.format("Cannot find the Service Provider by the resource ID: %s tenant domain: %s",
                                optApplicationConfig.getServiceProviderResourceId(), context.getTenantDomain()));
            }
            ApplicationConfig appConfig = new ApplicationConfig(serviceProvider);
            appConfig.setMappedSubjectIDSelected(optApplicationConfig.isMappedSubjectIDSelected());
            appConfig.setClaimMappings(optApplicationConfig.getClaimMappings());
            appConfig.setRoleMappings(optApplicationConfig.getRoleMappings());
            appConfig.setMandatoryClaims(optApplicationConfig.getMandatoryClaims());
            appConfig.setRequestedClaims(optApplicationConfig.getRequestedClaims());
            context.getSequenceConfig().setApplicationConfig(appConfig);
        }
    }

    private ServiceProvider reconstructServiceProvider(OptimizedApplicationConfig optApplicationConfig,
                                                       String tenantDomain)
            throws AuthenticationContextOptimizationException {

        ServiceProvider serviceProvider;
        try {
            serviceProvider = ApplicationMgtSystemConfig.getInstance().getApplicationDAO().
                    getApplicationByResourceId(optApplicationConfig.getServiceProviderResourceId(), tenantDomain);
            if (serviceProvider == null) {
                return null;
            }
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    optApplicationConfig.getAuthenticationSteps(tenantDomain));
        } catch (IdentityApplicationManagementException | FrameworkException e) {
            throw new AuthenticationContextOptimizationException(
                    String.format("Error occurred while retrieving the service provider by resource id: %s " +
                            "tenant domain: %s", optApplicationConfig.getServiceProviderResourceId(), tenantDomain), e);
        }
        return serviceProvider;
    }

    private IdentityProvider getIdPsByIdPName(String idPName, String tenantDomain)
            throws AuthenticationContextOptimizationException {

        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByName(idPName, tenantDomain);
            if (idp == null) {
                throw new AuthenticationContextOptimizationException(String.format(
                        "Cannot find the Identity Provider by the name: %s tenant domain: %s", idPName, tenantDomain));
            }
        } catch (IdentityProviderManagementException e) {
            throw new AuthenticationContextOptimizationException(String.format(
                    "Failed to get the Identity Provider by name: %s tenant domain: %s", idPName, tenantDomain), e);
        }
        return idp;
    }

    private IdentityProvider getIdPsByResourceID(String resourceId, String tenantDomain)
            throws AuthenticationContextOptimizationException {

        if (resourceId == null) {
            throw new AuthenticationContextOptimizationException("Error occurred while getting IdPs");
        }
        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByResourceId(resourceId, tenantDomain, false);
            if (idp == null) {
                throw new AuthenticationContextOptimizationException(
                        String.format("Cannot find the Identity Provider by the resource ID: %s " +
                                "tenant domain: %s", resourceId, tenantDomain));
            }
        } catch (IdentityProviderManagementException e) {
            throw new AuthenticationContextOptimizationException(
                    String.format("Failed to get the Identity Provider by resource id: %s tenant domain: %s",
                            resourceId, tenantDomain), e);
        }
        return idp;
    }
}
