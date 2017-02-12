package org.wso2.carbon.identity.gateway.deployer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceComponent;

import java.net.MalformedURLException;
import java.net.URL;

public class IdentityProviderDeployer implements Deployer {

    private ArtifactType artifactType;
    private URL repository;


    private Logger logger = LoggerFactory.getLogger(FrameworkServiceComponent.class);

    @Override
    public void init() {
        artifactType = new ArtifactType<>("policy");
        try {
            repository = new URL("file:/home/harshat/wso2/repo/is/untitled3");
        } catch (MalformedURLException e) {
        }
    }

    @Override
    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        readArtifact(artifact);
        return artifact.getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key + "is not a String value");
        }
        logger.debug("Undeploying : " + key);

    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Updating : " + artifact.getName());
        readArtifact(artifact);
        return artifact.getName();
    }

    @Override
    public URL getLocation() {

        logger.debug("Updating : "  );
        return repository;
    }

    @Override
    public ArtifactType getArtifactType() {

        logger.debug("Updating : "  );
        return artifactType;
    }

    /**
     * Read the artifacts and save the policy and metadata to PolicyStore and PolicyCollection
     * @param artifact deployed articles
     */
    private synchronized void readArtifact(Artifact artifact) {
        String artifactName = artifact.getName();

        logger.debug("Updating : " + artifact.getName());
    }
}
