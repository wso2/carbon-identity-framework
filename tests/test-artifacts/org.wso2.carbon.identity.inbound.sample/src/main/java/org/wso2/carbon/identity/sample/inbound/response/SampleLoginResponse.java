package org.wso2.carbon.identity.sample.inbound.response;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;

public class SampleLoginResponse extends GatewayResponse {

    private String subject;

    public String getSubject() {
        return subject;
    }

    protected SampleLoginResponse(GatewayResponseBuilder builder) {
        super(builder);
        this.subject = ((SampleLoginResponseBuilder) builder).subject;
    }

    public static class SampleLoginResponseBuilder extends GatewayResponseBuilder {

        public SampleLoginResponseBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        private String subject;

        public SampleLoginResponseBuilder(GatewayMessageContext context) {
            super(context);
        }

        @Override
        public GatewayResponse build() {
            return new SampleLoginResponse(this);
        }

    }

}
