package org.wso2.carbon.identity.entitlement.endpoint.filter;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.endpoint.exception.AbstractEntitlementException;
import org.wso2.carbon.identity.entitlement.endpoint.exception.RequestParseException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXB;

/**
 * Created by manujith on 7/26/16.
 */
@Provider
@Produces(MediaType.TEXT_PLAIN)
public class EntitlementExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {

        if(e instanceof AbstractEntitlementException){
            AbstractEntitlementException entitlementException = (AbstractEntitlementException)e;
//            return Response.status(entitlementException.getCode())
//                           .entity(entitlementException.getDescription()).build();
            return Response.status(Response.Status.OK).entity(entitlementException.getExceptioBean())
                                                      .build();

        }
        //return Response.status(Response.Status.BAD_REQUEST).entity("Error in request").build();
        return Response.status(Response.Status.OK).entity(new RequestParseException().getExceptioBean())
                                                  .build();
    }
}
