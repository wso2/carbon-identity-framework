package org.wso2.carbon.identity.sample.outbound.request;

import org.wso2.carbon.identity.gateway.processor.request.CallbackAuthenticationRequest;

public class SampleACSRequest extends CallbackAuthenticationRequest {

    protected SampleACSRequest(SampleACSRequestBuilder builder) {
        super(builder);
    }

    public static class SampleACSRequestBuilder extends CallbackAuthenticationRequest.CallbackAuthenticationRequestBuilder {

        public SampleACSRequestBuilder setSAML2SSOResponse(String saml2SSOResponse) {
            return this;
        }

    }

}
