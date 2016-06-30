
package org.wso2.carbon.identity.application.authentication.endpoint.client;

import org.wso2.carbon.identity.application.authentication.endpoint.util.bean.SelfRegistrationRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/self")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface SelfUserRegistrationResource{
    @PUT
    @Path("/resend")
    public Response regenerateCode(SelfRegistrationRequest selfRegistrationRequest);

}
