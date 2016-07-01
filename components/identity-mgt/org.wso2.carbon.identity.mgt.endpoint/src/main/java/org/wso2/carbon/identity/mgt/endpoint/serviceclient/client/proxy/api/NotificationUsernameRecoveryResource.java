
package org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/username")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public interface NotificationUsernameRecoveryResource {

    @GET
    @Path("/claims")
    public Response getAllLocalSupportedClaims();

}
