package org.wso2.carbon.identity.functions.library.mgt;


import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.functions.library.mgt.dao.FunctionLibraryDAO;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.model.FunctionLibrary;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.testutil.powermock.PowerMockIdentityBaseTest;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

//@RunWith(PowerMockRunner.class)
@PrepareForTest({IdentityTenantUtil.class,CarbonContext.class})
public class FunctionLibraryManagementAdminServiceTest extends PowerMockIdentityBaseTest{

        private static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
        private static final String SAMPLE_TENANT_DOMAIN2 = "abc.com";

        @BeforeMethod
        public void setUp(){
            mockStatic(IdentityTenantUtil.class);

            when(IdentityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN)).thenReturn(-1234);
            when(IdentityTenantUtil.getTenantDomain(-1234)).thenReturn(SAMPLE_TENANT_DOMAIN);

            when(IdentityTenantUtil.getTenantId(SAMPLE_TENANT_DOMAIN2)).thenReturn(-123);
            when(IdentityTenantUtil.getTenantDomain(-123)).thenReturn(SAMPLE_TENANT_DOMAIN2);
        }

        @DataProvider(name = "createFunctionLibraryDataProvider")
        public Object[][] createFunctionLibraryData() {
            FunctionLibrary functionLibrary1 = new FunctionLibrary();
            functionLibrary1.setFunctionLibraryName("");
            functionLibrary1.setDescription("sample1");
            functionLibrary1.setFunctionLibraryScript("samplefunction1");

            FunctionLibrary functionLibrary2 = new FunctionLibrary();
            functionLibrary2.setFunctionLibraryName("sample2");
            functionLibrary2.setDescription("sample2");
            functionLibrary2.setFunctionLibraryScript("samplefunction2");


            return new Object[][]{
                    {
                            functionLibrary1,
                            SAMPLE_TENANT_DOMAIN
                    }
//                    ,
//                    {
//                            functionLibrary2,
//                            SAMPLE_TENANT_DOMAIN2
//                    },
            };
        }


//        @Test(dataProvider = "createFunctionLibraryDataProvider",expectedExceptions = FunctionLibraryManagementException.class)
//        public void createFunctionLibrary(Object functionLibrary, String tenantDomain) throws FunctionLibraryManagementException {
//
//
//                //when(context.getTenantDomain()).thenReturn(tenantDomain);
//                FunctionLibraryDAO functionLibraryDAO= mock(FunctionLibraryDAO.class);
//                AbstractAdmin abstractAdmin =mock(AbstractAdmin.class);
//
//                when(functionLibraryDAO.isFunctionLibraryExists("sample1",SAMPLE_TENANT_DOMAIN)).thenReturn(true);
//               // when(functionLibraryDAO.isFunctionLibraryExists("sample2",SAMPLE_TENANT_DOMAIN)).thenReturn(false);
//                //when(getTenantDomain()).thenReturn(tenantDomain);
//
//               // PowerMockito.doReturn(tenantDomain).when(PowerMockito.spy(AbstractAdmin.n),(AbstractAdmin.class,"getTenantDomain"));
//            //AbstractAdmin abstractAdmin = mock(AbstractAdmin.class);
////                CarbonContext context =mock(CarbonContext.class);
////            Mockito.when(((CarbonContext)context).getTenantDomain()).thenReturn(tenantDomain);
//            //Mockito.when(((AbstractAdmin)abstractAdmin)).thenReturn(tenantDomain);
//
////            AbstractAdmin clazz = mock(AbstractAdmin.class, CALLS_REAL_METHODS);
////            when(clazz.getTenantDomain().thenReturn(tenantDomain);
//
//
//
//                FunctionLibraryManagementAdminService functionLibraryManagementAdminService = new FunctionLibraryManagementAdminService();
//
//                functionLibraryManagementAdminService.createFunctionLibrary((FunctionLibrary) functionLibrary);
//
//            }
}