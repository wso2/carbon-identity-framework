package org.wso2.carbon.identity.mgt.endpoint.serviceclient;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ConfirmSelfRegistrationRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.SelfRegistrationRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.NotificationUsernameRecoveryResource;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.SelfUserRegistrationResource;

import java.util.Map;
import javax.ws.rs.core.Response;

public class UserRegistrationClient {
    StringBuilder builder = new StringBuilder();
    String url = IdentityManagementEndpointUtil.buildEndpointUrl(
                     IdentityManagementEndpointConstants.UserInfoRecovery.REST_API_URL_DOMAIN);

    public Response getAllClaims(String tenantDomain) {
        NotificationUsernameRecoveryResource notificationUsernameRecoveryResource = JAXRSClientFactory
                .create(url, NotificationUsernameRecoveryResource.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response responseObj = notificationUsernameRecoveryResource.getAllLocalSupportedClaims();
        return responseObj;
    }
    public Response registerUser(SelfRegistrationRequest registrationRequest, Map<String, String> headers) {
        SelfUserRegistrationResource selfUserRegistrationResource = IdentityManagementEndpointUtil
                .create(url, SelfUserRegistrationResource.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider(), null, headers);
        Response responseObj = selfUserRegistrationResource.registerUser(registrationRequest);
        return responseObj;
    }

    public Response confirmUser(ConfirmSelfRegistrationRequest confirmSelfRegistrationRequest) {
        SelfUserRegistrationResource selfUserRegistrationResource = JAXRSClientFactory
                .create(url, SelfUserRegistrationResource.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response responseObj = selfUserRegistrationResource.confirmCode(confirmSelfRegistrationRequest);
        return responseObj;
    }
}
