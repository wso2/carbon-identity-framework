package org.wso2.carbon.identity.application.authentication.framework.inbound;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InboundAuthenticationContext implements Serializable {

	private static final long serialVersionUID = -3113147804821962230L;

	private InboundAuthenticationRequest inboundAuthenticationRequest;
    private InboundAuthenticationResponse inboundAuthenticationResponse;
	private String tenantDomain;
	private Map<String, Object> properties = new HashMap<String, Object>();

	public InboundAuthenticationRequest getInboundAuthenticationRequest() {
		return inboundAuthenticationRequest;
	}

	public void setInboundAuthenticationRequest(InboundAuthenticationRequest inboundAuthenticationRequest) {
		this.inboundAuthenticationRequest = inboundAuthenticationRequest;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public void addProperty(String key, Object value) {
		properties.put(key, value);
	}

	public String getTenantDomain() {
		return tenantDomain;
	}

	public void setTenantDomain(String tenantDomain) {
		this.tenantDomain = tenantDomain;
	}

    public InboundAuthenticationResponse getInboundAuthenticationResponse() {
        return inboundAuthenticationResponse;
    }

    public void setInboundAuthenticationResponse(InboundAuthenticationResponse inboundAuthenticationResponse) {
        this.inboundAuthenticationResponse = inboundAuthenticationResponse;
    }
}
