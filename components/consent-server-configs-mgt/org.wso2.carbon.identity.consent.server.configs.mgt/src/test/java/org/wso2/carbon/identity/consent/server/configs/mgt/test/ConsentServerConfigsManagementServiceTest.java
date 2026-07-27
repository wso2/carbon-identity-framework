/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.consent.server.configs.mgt.test;


import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.consent.server.configs.mgt.exceptions.ConsentServerConfigsMgtException;
import org.wso2.carbon.identity.consent.server.configs.mgt.internal.ConsentServerConfigsManagementDataHolder;
import org.wso2.carbon.identity.consent.server.configs.mgt.services.ConsentServerConfigsManagementService;
import org.wso2.carbon.identity.consent.server.configs.mgt.services.ConsentServerConfigsManagementServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.ENFORCE_EXTERNAL_CONSENT_PAGE;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.EXTERNAL_CONSENT_PAGE;
import static org.wso2.carbon.identity.consent.server.configs.mgt.utils.Constants.EXTERNAL_CONSENT_PAGE_URL;

public class ConsentServerConfigsManagementServiceTest {

    public static final String EXTERNAL_CONSENT_PAGE_URL_VALUE = "https://localhost:9443/consent-mgt/consent";
    @Mock
    ConfigurationManager configurationManager;
    private ConsentServerConfigsManagementService serverConfigsService;


    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        serverConfigsService = new ConsentServerConfigsManagementServiceImpl();
        ConsentServerConfigsManagementDataHolder.setConfigurationManager(configurationManager);
    }

    @DataProvider(name = "handleResourceDataProvider")
    public Object[][] handleResourceDataProvider() {

        return new Object[][]{
                {"tenant1", getResources()},
                {"tenant2", null}
        };
    }

    @Test(dataProvider = "handleResourceDataProvider")
    public void getExternalConsentPageUrlTest(String tenantDomain, Resources resources) throws Exception {

        try {
            when(configurationManager.getTenantResources(anyString(), any(Condition.class))).thenReturn(resources);
            String externalConsentPageUrl = serverConfigsService.getExternalConsentPageUrl(tenantDomain);
            Assert.assertEquals(externalConsentPageUrl, EXTERNAL_CONSENT_PAGE_URL_VALUE);
        } catch (ConsentServerConfigsMgtException e) {
            Assert.assertTrue(e.getMessage().contains("External consent page configurations are not found."));
        }
    }

    @DataProvider(name = "enforceExternalConsentPageDataProvider")
    public Object[][] enforceExternalConsentPageDataProvider() {

        return new Object[][]{
                // enforce=true, url present -> true
                {"tenant1", getResources("true", EXTERNAL_CONSENT_PAGE_URL_VALUE), true},
                // enforce=true, url missing -> false (falls back due to missing url)
                {"tenant2", getResources("true", ""), false},
                // enforce=true, url blank -> false (falls back due to blank url)
                {"tenant2_1", getResources("true", "   "), false},
                // enforce=false, url present -> false
                {"tenant3", getResources("false", EXTERNAL_CONSENT_PAGE_URL_VALUE), false},
                // enforce missing, url present -> false
                {"tenant4", getResources(null, EXTERNAL_CONSENT_PAGE_URL_VALUE), false},
                // null resources -> false (exception caught)
                {"tenant5", null, false}
        };
    }

    @Test(dataProvider = "enforceExternalConsentPageDataProvider")
    public void isExternalConsentPageEnforcedTest(String tenantDomain, Resources resources,
                                                   boolean expectedResult) throws Exception {

        when(configurationManager.getTenantResources(anyString(), any(Condition.class))).thenReturn(resources);
        boolean isEnforced = serverConfigsService.isExternalConsentPageEnforced(tenantDomain);
        Assert.assertEquals(isEnforced, expectedResult);
    }

    private Resources getResources() {

        return getResources(null, EXTERNAL_CONSENT_PAGE_URL_VALUE);
    }

    private Resources getResources(String enforceValue, String urlValue) {

        Resources resources = new Resources();
        List<Resource> resourceList = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();

        if (enforceValue != null) {
            Attribute enforceAttribute = new Attribute();
            enforceAttribute.setKey(ENFORCE_EXTERNAL_CONSENT_PAGE);
            enforceAttribute.setValue(enforceValue);
            attributes.add(enforceAttribute);
        }

        if (urlValue != null) {
            Attribute urlAttribute = new Attribute();
            urlAttribute.setKey(EXTERNAL_CONSENT_PAGE_URL);
            urlAttribute.setValue(urlValue);
            attributes.add(urlAttribute);
        }

        Resource resource = new Resource();
        resource.setAttributes(attributes);
        resource.setResourceName(EXTERNAL_CONSENT_PAGE);
        resourceList.add(resource);
        resources.setResources(resourceList);
        return resources;
    }

}
