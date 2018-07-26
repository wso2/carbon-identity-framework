package org.wso2.carbon.identity.api.idpmgt.endpoint.factories;

import org.wso2.carbon.identity.api.idpmgt.endpoint.IdpsApiService;
import org.wso2.carbon.identity.api.idpmgt.endpoint.impl.IdpsApiServiceImpl;

public class IdpsApiServiceFactory {

   private final static IdpsApiService service = new IdpsApiServiceImpl();

   public static IdpsApiService getIdpsApi()
   {
      return service;
   }
}
