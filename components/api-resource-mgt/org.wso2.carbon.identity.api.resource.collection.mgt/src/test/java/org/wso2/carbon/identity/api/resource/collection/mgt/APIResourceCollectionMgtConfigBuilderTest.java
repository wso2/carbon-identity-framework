package org.wso2.carbon.identity.api.resource.collection.mgt;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionMgtConfigBuilder;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.event.IdentityEventException;

import java.util.Map;

@WithCarbonHome
@WithAxisConfiguration
public class APIResourceCollectionMgtConfigBuilderTest {

    APIResourceCollectionMgtConfigBuilder configBuilder;

    @BeforeMethod
    public void setUp() throws IdentityEventException {
        configBuilder = APIResourceCollectionMgtConfigBuilder.getInstance();
    }

    @Test
    public void testGetApIResourceCollections() {

        APIResourceCollectionMgtConfigBuilder instance = APIResourceCollectionMgtConfigBuilder.getInstance();
        Map<String, APIResourceCollection> apiResourceCollectionMap = instance
                .getApiResourceCollectionMgtConfigurations();
        Assert.assertNotNull(apiResourceCollectionMap);
        Assert.assertFalse(apiResourceCollectionMap.isEmpty());
    }
}
