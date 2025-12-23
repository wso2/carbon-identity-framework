/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.store.configuration;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;

import java.nio.file.Paths;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Test class for UserStoreConfigServiceImpl.
 */
public class UserStoreConfigServiceImplTest {

    private UserStoreConfigServiceImpl userStoreConfigService;

    private static final String SAMPLE_DOMAIN_ID = "PRIMARY";
    private static final String FILE_BASED_REPOSITORY_CLASS =
            "org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory";


    @BeforeMethod
    public void setUp() throws Exception {

        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        userStoreConfigService = new UserStoreConfigServiceImpl();
    }


    private UserStoreDTO buildUserStoreDTO(PropertyDTO[] properties) {

        UserStoreDTO dto = new UserStoreDTO();
        dto.setDomainId(UserStoreConfigServiceImplTest.SAMPLE_DOMAIN_ID);
        dto.setRepositoryClass(FILE_BASED_REPOSITORY_CLASS);
        dto.setDescription("Updated Description");
        dto.setProperties(properties);
        return dto;
    }

    @Test
    public void testUpdateUserStore() throws Exception {

        UserStoreDTO updatedDto = buildUserStoreDTO(new PropertyDTO[]{});

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<SecondaryUserStoreConfigurationUtil> secondaryUserStoreConfigUtilMockedStatic
                     = mockStatic(SecondaryUserStoreConfigurationUtil.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {

            UserStoreDAO mockUserStoreDAO = mock(UserStoreDAO.class);
            mockCarbonContext(privilegedCarbonContext, carbonContext);

            mockSecondaryUserStoreConfigUtil(mockUserStoreDAO, secondaryUserStoreConfigUtilMockedStatic);
            doNothing().when(mockUserStoreDAO).updateUserStore(updatedDto, false);

            userStoreConfigService.updateUserStore(updatedDto, true);
        }

    }

    @Test
    public void testUpdateDisabledUserStore() throws Exception {

        PropertyDTO propertyDTO = new PropertyDTO("Disabled", "true");
        UserStoreDTO updatedDto = buildUserStoreDTO(new PropertyDTO[]{propertyDTO});

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<SecondaryUserStoreConfigurationUtil> secondaryUserStoreConfigUtilMockedStatic
                     = mockStatic(SecondaryUserStoreConfigurationUtil.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {

            UserStoreDAO mockUserStoreDAO = mock(UserStoreDAO.class);
            mockCarbonContext(privilegedCarbonContext, carbonContext);

            mockSecondaryUserStoreConfigUtil(mockUserStoreDAO, secondaryUserStoreConfigUtilMockedStatic);
            doNothing().when(mockUserStoreDAO).updateUserStore(updatedDto, false);

            userStoreConfigService.updateUserStore(updatedDto, true);
        }

    }

    private void mockSecondaryUserStoreConfigUtil(UserStoreDAO mockUserStoreDAO,
            MockedStatic<SecondaryUserStoreConfigurationUtil> secondaryUserStoreConfigUtil) {

        secondaryUserStoreConfigUtil.when(SecondaryUserStoreConfigurationUtil::getFileBasedUserStoreDAOFactory)
                .thenReturn(mockUserStoreDAO);
    }

    private void mockCarbonContext(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext,
                                   MockedStatic<CarbonContext> carbonContext) {

        initPrivilegedCarbonContext(privilegedCarbonContext);
        CarbonContext mockCarbonContext = mock(CarbonContext.class);
        carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
    }

    private void initPrivilegedCarbonContext(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext) {

        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(
                PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");
    }
}
