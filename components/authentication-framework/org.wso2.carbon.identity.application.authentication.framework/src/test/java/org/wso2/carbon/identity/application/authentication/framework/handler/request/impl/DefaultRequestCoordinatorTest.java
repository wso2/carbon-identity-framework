package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.LOGOUT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TYPE;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.TENANT_NAME_FROM_CONTEXT;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Unit tests for {@link DefaultRequestCoordinator}.
 */
@WithCarbonHome
public class DefaultRequestCoordinatorTest extends IdentityBaseTest {

    private DefaultRequestCoordinator requestCoordinator;

    @BeforeMethod
    public void setUp() throws Exception {

        requestCoordinator = new DefaultRequestCoordinator();
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @DataProvider(name = "tenantDomainProvider")
    public Object[][] provideTenantDomain() {

        return new Object[][]{
                {null, null, SUPER_TENANT_DOMAIN_NAME},
                {"foo.com", "xyz.com", "foo.com"},
                {null, "xyz.com", "xyz.com"},
        };
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testTenantDomainInAuthenticationContext(String tenantDomainInThreadLocal,
                                                        String tenantDomainInRequestParam,
                                                        String expected) throws Exception {

        setTenantDomainInThreadLocalContext(tenantDomainInThreadLocal);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TYPE)).thenReturn("oauth");
        when(request.getParameter(LOGOUT)).thenReturn("true");
        when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomainInRequestParam);

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthenticationContext context = requestCoordinator.initializeFlow(request, response);

        assertEquals(context.getTenantDomain(), expected);
    }

    private void setTenantDomainInThreadLocalContext(String tenantDomainInThreadLocalContext) {

        IdentityUtil.threadLocalProperties.get().put(TENANT_NAME_FROM_CONTEXT, tenantDomainInThreadLocalContext);
    }
}
