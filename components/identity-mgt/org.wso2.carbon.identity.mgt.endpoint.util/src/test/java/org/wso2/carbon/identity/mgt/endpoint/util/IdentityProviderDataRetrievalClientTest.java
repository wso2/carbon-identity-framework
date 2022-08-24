package org.wso2.carbon.identity.mgt.endpoint.util;

import junit.framework.TestCase;
import org.jaxen.util.SingletonList;
import org.wso2.carbon.identity.mgt.endpoint.util.client.IdentityProviderDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.IdentityProviderDataRetrievalClientException;

import java.util.Map;

public class IdentityProviderDataRetrievalClientTest extends TestCase {

    @Override
    protected void setUp() throws Exception {

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    public void testGetFederatedIdpConfigs() {

        IdentityProviderDataRetrievalClient client = new IdentityProviderDataRetrievalClient();
        try {
            Map<String, String> clientId = client.getFederatedIdpConfigs("carbon.super", "Google",
                    "GoogleOIDCAuthenticator", new SingletonList("ClientId"));
            assertNotNull(clientId);
        } catch (IdentityProviderDataRetrievalClientException e) {
            throw new RuntimeException(e);
        }
    }
}
