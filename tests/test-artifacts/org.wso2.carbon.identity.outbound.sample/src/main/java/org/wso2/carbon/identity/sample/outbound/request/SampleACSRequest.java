package org.wso2.carbon.identity.sample.outbound.request;

import org.wso2.carbon.identity.gateway.processor.request.CallbackAuthenticationRequest;

public class SampleACSRequest extends CallbackAuthenticationRequest {

    public static class SAML2ACSRequestBuilder extends CallbackAuthenticationRequest.CallbackAuthenticationRequestBuilder {

        protected String saml2SSOResponse;

        public SAML2ACSRequestBuilder setSAML2SSOResponse(String saml2SSOResponse) {
            this.saml2SSOResponse = saml2SSOResponse;
            return this;
        }

    }

}
