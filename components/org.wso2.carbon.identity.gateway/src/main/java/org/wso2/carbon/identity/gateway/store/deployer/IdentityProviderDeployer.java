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
package org.wso2.carbon.identity.gateway.store.deployer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderEntity;
import org.wso2.carbon.identity.gateway.store.IdentityProviderConfigStore;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * IdentityProviderDeployer is the one that sync with the deployment path to load the service
 * provider files in to the store.
 */
public class IdentityProviderDeployer implements Deployer {

    private static final String IDENTITY_PROVIDER_TYPE = "identityprovider";
    private Logger logger = LoggerFactory.getLogger(IdentityProviderDeployer.class);
    private ArtifactType artifactType;
    private URL repository;

    @Override
    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        if (logger.isDebugEnabled()) {
            logger.debug("Deploying IdentityProvider configs.");
        }
        try {
            IdentityProviderConfig IdentityProviderConfig = getIdentityProviderConfig(artifact);
            IdentityProviderConfigStore.getInstance().addIdentityProvider(IdentityProviderConfig);
        } catch (GatewayServerException e) {
            String errorMessage = "Error occurred while deploying Identity Provider. " + e.getMessage();
            logger.error(errorMessage);
            throw new CarbonDeploymentException(errorMessage, e);
        }

        logger.info("Successfully deployed the IdentityProvider configs : " + artifact.getName());
        return artifact.getName();
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    /**
     * Read Identity Provider Config Object.
     *
     * @param artifact
     * @return IdentityProviderConfig
     */
    public synchronized IdentityProviderConfig getIdentityProviderConfig(Artifact artifact)
            throws GatewayServerException {
        if (logger.isDebugEnabled()) {
            logger.debug("Read Identity Provider Configs.");
        }
        String providerName = GatewayUtil.getProviderName(artifact.getName());
        IdentityProviderEntity providerEntity = GatewayUtil.getProvider(artifact, IdentityProviderEntity.class);
        if (providerEntity == null) {
            throw new GatewayServerException("Provider name cannot be found.");
        }
        if (!providerEntity.getIdentityProviderConfig().getName().equals(providerName)) {

            throw new GatewayServerException("Provider name should be the same as file name.");
        }
        return providerEntity.getIdentityProviderConfig();
    }

    @Override
    public URL getLocation() {
        return repository;
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing IdentityProviderDeployer.");
        }
        artifactType = new ArtifactType<>(IDENTITY_PROVIDER_TYPE);
        Path path = Paths.get(System.getProperty("carbon.home", "."), "deployment", "identityprovider");
        try {
            repository = new URL("file:" + path.toString());
        } catch (MalformedURLException e) {
            logger.error("Error while reading the file path : " + e.getMessage());
        }
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (logger.isDebugEnabled()) {
            logger.debug("Un-Deploying IdentityProvider configs.");
        }
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un-Deploying : " + key + "is not a String value");
        }
        try {
            String providerName = GatewayUtil.getProviderName((String) key);
            IdentityProviderConfigStore.getInstance().removeIdentityProvider(providerName);
        } catch (GatewayServerException e) {
            String errorMessage = "Error occurred while Un-Deploying the Identity Provider. " + e.getMessage();
            throw new CarbonDeploymentException(errorMessage, e);
        }
        logger.info("Successfully Un-Deployed the IdentityProvider configs : " + key);
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating IdentityProvider configs.");
        }
        try {
            IdentityProviderConfig IdentityProviderConfig = getIdentityProviderConfig(artifact);
            IdentityProviderConfigStore.getInstance().addIdentityProvider(IdentityProviderConfig);
        } catch (GatewayServerException e) {
            String errorMessage = "Error occurred while updating the Identity Provider. " + e.getMessage();
            throw new CarbonDeploymentException(errorMessage, e);
        }
        logger.info("Successfully updated the IdentityProvider configs : " + artifact.getName());
        return artifact.getName();
    }
}




