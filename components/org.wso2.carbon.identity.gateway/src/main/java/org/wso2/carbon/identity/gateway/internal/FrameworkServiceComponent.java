/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCIdentityContextDAO;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCSessionDAO;
import org.wso2.carbon.identity.gateway.deployer.IdentityProviderDeployer;
import org.wso2.carbon.identity.gateway.deployer.ServiceProviderDeployer;
import org.wso2.carbon.identity.gateway.processor.AuthenticationProcessor;
import org.wso2.carbon.identity.gateway.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.DefaultSequenceBuilderFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestValidator;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.service.GatewayClaimResolverService;
import org.wso2.carbon.identity.gateway.store.IdentityProviderConfigStore;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;
import org.wso2.carbon.identity.mgt.RealmService;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;


@Component(
        name = "org.wso2.carbon.identity.gateway.internal.FrameworkServiceComponent",
        immediate = true
)
public class FrameworkServiceComponent {

    private Logger log = LoggerFactory.getLogger(FrameworkServiceComponent.class);


    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        //Registering processor
        AuthenticationProcessor authenticationProcessor = new AuthenticationProcessor();
        bundleContext.registerService(GatewayProcessor.class, authenticationProcessor, null);


        //Registering this for demo perposes only
        bundleContext.registerService(AbstractSequenceBuildFactory.class, new DefaultSequenceBuilderFactory(), null);
        bundleContext.registerService(AuthenticationHandler.class, new AuthenticationHandler(), null);
        bundleContext.registerService(SequenceManager.class, new SequenceManager(), null);
        bundleContext.registerService(RequestPathHandler.class, new RequestPathHandler(), null);
        bundleContext.registerService(StepHandler.class, new StepHandler(), null);

        bundleContext.registerService(Deployer.class, new ServiceProviderDeployer(), null);
        bundleContext.registerService(Deployer.class, new IdentityProviderDeployer(), null);
        bundleContext.registerService(ServiceProviderConfigStore.class, ServiceProviderConfigStore.getInstance(), null);
        bundleContext.registerService(IdentityProviderConfigStore.class, IdentityProviderConfigStore.getInstance(), null);
        bundleContext.registerService(GatewayClaimResolverService.class, GatewayClaimResolverService.getInstance(), null);

        //bundleContext.registerService(GatewayRequestBuilderFactory.class, new LocalAuthenticationRequestBuilderFactory(), null);
        //FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);


        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    @Deactivate
    protected void stop() throws Exception {

    }


    @Reference(
            name = "application.authenticator",
            service = ApplicationAuthenticator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAuthenticator"
    )
    protected void setAuthenticator(ApplicationAuthenticator authenticator) {

        if (authenticator instanceof LocalApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators()
                    .add((LocalApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators()
                    .add((FederatedApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators()
                    .add((RequestPathApplicationAuthenticator) authenticator);
        } else {
            log.error("Unsupported Authenticator found : " + authenticator.getName());
        }

        if (log.isDebugEnabled()) {
            log.debug("Added application authenticator : " + authenticator.getName());
        }
    }

    protected void unSetAuthenticator(ApplicationAuthenticator authenticator) {

        if (authenticator instanceof LocalApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators().remove(authenticator);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed application authenticator : " + authenticator.getName());
        }

    }


    @Reference(
            name = "identity.handlers.request",
            service = AbstractRequestValidator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetRequestHandler"
    )
    protected void addRequestHandler(AbstractRequestValidator abstractRequestValidator) {

            FrameworkServiceDataHolder.getInstance().getRequestHandlers().add(abstractRequestValidator);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + abstractRequestValidator.getName());
        }
    }

    protected void unSetRequestHandler(AbstractRequestValidator abstractRequestValidator) {

        FrameworkServiceDataHolder.getInstance().getRequestHandlers().remove(abstractRequestValidator);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + abstractRequestValidator.getName());
        }
    }

    @Reference(
            name = "identity.handlers.authentication",
            service = AuthenticationHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAuthenticationHandler"
    )
    protected void addAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers().add(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + authenticationHandler.getName());
        }
    }

    protected void unSetAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers().remove(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + authenticationHandler.getName());
        }
    }



    @Reference(
            name = "identity.handlers.response",
            service = AbstractResponseHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetResponseHandler"
    )
    protected void addResponseHandler(AbstractResponseHandler responseHandler) {

        FrameworkServiceDataHolder.getInstance().getResponseHandlers().add(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractResponseHandler : " + responseHandler.getName());
        }
    }

    protected void unSetResponseHandler(AbstractResponseHandler responseHandler) {

        FrameworkServiceDataHolder.getInstance().getResponseHandlers().remove(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractResponseHandler : " + responseHandler.getName());
        }
    }

    @Reference(
            name = "identity.handlers.sequence.factory",
            service = AbstractSequenceBuildFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetSequenceBuildFactory"
    )
    protected void addSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {

        FrameworkServiceDataHolder.getInstance().getSequenceBuildFactories().add(sequenceBuildFactory);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
        }
    }

    protected void unSetSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {

        FrameworkServiceDataHolder.getInstance().getSequenceBuildFactories().remove(sequenceBuildFactory);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
        }
    }

    @Reference(
            name = "identity.handlers.sequence.manager",
            service = SequenceManager.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetSequenceManager"
    )
    protected void addSequenceManager(SequenceManager sequenceManager) {

        FrameworkServiceDataHolder.getInstance().getSequenceManagers().add(sequenceManager);

        if (log.isDebugEnabled()) {
            log.debug("Added SequenceManager : " + sequenceManager.getName());
        }
    }

    protected void unSetSequenceManager(SequenceManager sequenceManager) {

        FrameworkServiceDataHolder.getInstance().getSequenceManagers().remove(sequenceManager);

        if (log.isDebugEnabled()) {
            log.debug("Removed SequenceManager : " + sequenceManager.getName());
        }
    }

    @Reference(
            name = "identity.handlers.requestpath",
            service = RequestPathHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetRequestPathHandler"
    )
    protected void addRequestPathHandler(RequestPathHandler requestPathHandler) {

        FrameworkServiceDataHolder.getInstance().getRequestPathHandlers().add(requestPathHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added RequestPathHandler : " + requestPathHandler.getName());
        }
    }

    protected void unSetRequestPathHandler(RequestPathHandler requestPathHandler) {

        FrameworkServiceDataHolder.getInstance().getRequestPathHandlers().remove(requestPathHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed RequestPathHandler : " + requestPathHandler.getName());
        }
    }

    @Reference(
            name = "identity.handlers.step",
            service = StepHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetStepHandler"
    )
    protected void addStepHandler(StepHandler stepHandler) {

        FrameworkServiceDataHolder.getInstance().getStepHandlers().add(stepHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added StepHandler : " + stepHandler.getName());
        }
    }

    protected void unSetStepHandler(StepHandler stepHandler) {

        FrameworkServiceDataHolder.getInstance().getStepHandlers().remove(stepHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed StepHandler : " + stepHandler.getName());
        }
    }

    @Reference(
            name = "identity.claim.resolving",
            service = ClaimResolvingService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetClaimResolvingService"
    )
    protected void addClaimResolvingService(ClaimResolvingService claimResolvingService) {

        FrameworkServiceDataHolder.getInstance().setClaimResolvingService(claimResolvingService);

        if (log.isDebugEnabled()) {
            log.debug("Added ClaimResolvingService : " + ClaimResolvingService.class);
        }
    }

    protected void unSetClaimResolvingService(ClaimResolvingService claimResolvingService) {

        FrameworkServiceDataHolder.getInstance().setClaimResolvingService(null);

        if (log.isDebugEnabled()) {
            log.debug("Removed ClaimResolvingService : " + ClaimResolvingService.class);
        }
    }



    @Reference(
            name = "identity.claim.profile",
            service = ProfileMgtService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetProfileMgtService"
    )
    protected void addProfileMgtService(ProfileMgtService profileMgtService) {

        FrameworkServiceDataHolder.getInstance().setProfileMgtService(profileMgtService);

        if (log.isDebugEnabled()) {
            log.debug("Added ProfileMgtService : " + ProfileMgtService.class);
        }
    }

    protected void unSetProfileMgtService(ProfileMgtService profileMgtService) {

        FrameworkServiceDataHolder.getInstance().setProfileMgtService(null);

        if (log.isDebugEnabled()) {
            log.debug("Removed ProfileMgtService : " + ProfileMgtService.class);
        }
    }

    @Reference(
            name = "org.wso2.carbon.datasource.jndi",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "onJNDIUnregister") protected void onJNDIReady(JNDIContextManager jndiContextManager) {
        try {
            Context ctx = jndiContextManager.newInitialContext();
            DataSource dsObject = (DataSource) ctx.lookup("java:comp/env/jdbc/WSO2CARBON_DB");
            if (dsObject != null) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dsObject);
                initializeDao(jdbcTemplate);
            } else {
                log.error("Could not find WSO2CarbonDB");
            }
        } catch (NamingException e) {
            log.error("Error occurred while looking up the Datasource", e);
        }
    }
    protected void onJNDIUnregister(JNDIContextManager jndiContextManager) {
        log.info("Un-registering data sources");
    }
    private void initializeDao(JdbcTemplate jdbcTemplate) {
        JDBCSessionDAO.getInstance().setJdbcTemplate(jdbcTemplate);
        JDBCIdentityContextDAO.getInstance().setJdbcTemplate(jdbcTemplate);
    }


    @Reference(
            name = "realmService",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        //dataHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        //dataHolder.setRealmService(null);
    }


}
