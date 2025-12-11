/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimManagerFactory;
import org.wso2.carbon.user.core.claim.DefaultClaimManager;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.claim.inmemory.FileBasedClaimBuilder;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

/**
 * An implementation of {@link org.wso2.carbon.user.core.claim.ClaimManagerFactory} interface to used with all
 * advanced claim management use cases.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.user.core.claim.ClaimManagerFactory",
                "service.scope=singleton"
        }
)
public class ClaimMetadataStoreFactory implements ClaimManagerFactory {

    private static final Log log = LogFactory.getLog(DefaultClaimManager.class);
    private static ClaimConfig claimConfig;

    static {
        try {
            claimConfig = FileBasedClaimBuilder.buildClaimMappingsFromConfigFile();
            IdentityClaimManagementServiceDataHolder.getInstance().setClaimConfig(claimConfig);
        } catch (IOException e) {
            log.error("Could not find claim configuration file", e);
        } catch (XMLStreamException e) {
            log.error("Error while parsing claim configuration file", e);
        } catch (UserStoreException e) {
            log.error("Error while initializing claim manager");
        }
    }

    @Override
    public synchronized ClaimManager createClaimManager(int tenantId) {
        DefaultClaimMetadataStore identityClaimManager = new DefaultClaimMetadataStore(claimConfig, tenantId);

        return identityClaimManager;
    }

}
