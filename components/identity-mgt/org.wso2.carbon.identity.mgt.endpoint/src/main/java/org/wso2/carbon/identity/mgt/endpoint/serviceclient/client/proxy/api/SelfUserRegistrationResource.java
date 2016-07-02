
package org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api;

import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ConfirmSelfRegistrationRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.SelfRegistrationRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/self")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public interface SelfUserRegistrationResource{

    @POST
    @Path("/register")
    public Response registerUser(SelfRegistrationRequest registrationRequest) ;

    @PUT
    @Path("/confirm")
    public Response confirmCode(ConfirmSelfRegistrationRequest confirmSelfRegistrationRequest);

}
