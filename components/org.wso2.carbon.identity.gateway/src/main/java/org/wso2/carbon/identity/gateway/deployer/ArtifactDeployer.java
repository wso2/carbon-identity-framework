package org.wso2.carbon.identity.gateway.deployer;


import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

import java.net.URL;

public class ArtifactDeployer implements Deployer {

    private static final String ARTIFACT_LOCATION = "/home/harshat/wso2/repo/is/untitled3/sample.yaml";

    @Override
    public void init() {

    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        return null;
    }

    @Override
    public void undeploy(Object o) throws CarbonDeploymentException {

    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        return null;
    }

    @Override
    public URL getLocation() {
        return null;
    }

    @Override
    public ArtifactType getArtifactType() {
        return null;
    }
}
