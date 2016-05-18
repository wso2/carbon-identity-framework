package org.wso2.carbon.identity.application.authentication.framework.processor.authenticator;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .AuthenticationResponse;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import javax.mail.AuthenticationFailedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractApplicationAuthenticator implements ApplicationAuthenticator{
    private static final long serialVersionUID = -4406878411547612129L;
    private static final Log log = LogFactory.getLog(AbstractApplicationAuthenticator.class);

    @Override
    public AuthenticationResponse process(AuthenticationContext authenticationContext) {
        return null;
    }

    @Override
    public boolean canHandle(AuthenticationContext authenticationContext) {
        return false;
    }

    @Override
    public String getContextIdentifier(AuthenticationContext authenticationContext) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public List<Property> getConfigurationProperties() {
        return null;
    }
}
