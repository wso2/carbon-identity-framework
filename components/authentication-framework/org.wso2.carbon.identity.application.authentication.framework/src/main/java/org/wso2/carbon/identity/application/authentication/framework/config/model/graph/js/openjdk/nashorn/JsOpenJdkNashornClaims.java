/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsClaims;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;
import java.util.Optional;

/**
 * Represent the user's claim. Can be either remote or local.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 *
 */
public class JsOpenJdkNashornClaims extends JsClaims implements AbstractOpenJdkNashornJsObject {

    private static final Log LOG = LogFactory.getLog(JsOpenJdkNashornClaims.class);

    /**
     * Constructor to get the user authenticated in step 'n'
     *
     * @param step                 The authentication step
     * @param idp                  The authenticated IdP
     * @param isRemoteClaimRequest Whether the request is for remote claim (false for local claim request)
     */
    public JsOpenJdkNashornClaims(AuthenticationContext context, int step, String idp, boolean isRemoteClaimRequest) {

        this(step, idp, isRemoteClaimRequest);
        initializeContext(context);
    }

    public JsOpenJdkNashornClaims(int step, String idp, boolean isRemoteClaimRequest) {

        this.isRemoteClaimRequest = isRemoteClaimRequest;
        this.idp = idp;
        this.step = step;
    }

    @Override
    public void initializeContext(AuthenticationContext context) {

        super.initializeContext(context);
        if (this.authenticatedUser == null) {
            if (StringUtils.isNotBlank(idp) && getContext().getCurrentAuthenticatedIdPs().containsKey(idp)) {
                this.authenticatedUser = getContext().getCurrentAuthenticatedIdPs().get(idp).getUser();
            } else {
                this.authenticatedUser = getAuthenticatedUserFromSubjectIdentifierStep();
            }
        }
    }

    /**
     * Get authenticated user from step config of current subject identifier.
     *
     * @return AuthenticatedUser.
     */
    private AuthenticatedUser getAuthenticatedUserFromSubjectIdentifierStep() {

        AuthenticatedUser authenticatedUser = null;
        StepConfig stepConfig = getCurrentSubjectIdentifierStep();
        if (stepConfig != null) {
            authenticatedUser = getCurrentSubjectIdentifierStep().getAuthenticatedUser();
        }
        return authenticatedUser;
    }

    /**
     * Retrieve step config of current subject identifier.
     *
     * @return StepConfig.
     */
    private StepConfig getCurrentSubjectIdentifierStep() {

        if (getContext().getSequenceConfig() == null) {
            // Sequence config is not yet initialized.
            return null;
        }
        Map<Integer, StepConfig> stepConfigs = getContext().getSequenceConfig().getStepMap();
        Optional<StepConfig> subjectIdentifierStep = stepConfigs.values().stream()
                .filter(stepConfig -> (stepConfig.isCompleted() && stepConfig.isSubjectIdentifierStep())).findFirst();
        if (subjectIdentifierStep.isPresent()) {
            return subjectIdentifierStep.get();
        } else if (getContext().getCurrentStep() > 0) {
            return stepConfigs.get(getContext().getCurrentStep());
        } else {
            return null;
        }
    }

    /**
     * Constructor to get user who is not directly from a authentication step. Eg. Associated user of authenticated
     * federated user in a authentication step.
     *
     * @param authenticatedUser    Authenticated user
     * @param isRemoteClaimRequest Whether the request is for remote claim (false for local claim request)
     */
    public JsOpenJdkNashornClaims(AuthenticatedUser authenticatedUser, boolean isRemoteClaimRequest) {

        this.isRemoteClaimRequest = isRemoteClaimRequest;
        this.authenticatedUser = authenticatedUser;
    }

    public JsOpenJdkNashornClaims(AuthenticationContext context, AuthenticatedUser authenticatedUser,
                                  boolean isRemoteClaimRequest) {

        this(authenticatedUser, isRemoteClaimRequest);
        initializeContext(context);
    }

    @Override
    public Object getMember(String claimUri) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                return getFederatedClaim(claimUri);
            } else {
                return getLocalClaim(claimUri);
            }
        }
        return null;
    }

    @Override
    public boolean hasMember(String claimUri) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                return hasFederatedClaim(claimUri);
            } else {
                return hasLocalClaim(claimUri);
            }
        }
        return false;
    }

    @Override
    public void setMember(String claimUri, Object claimValue) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                setFederatedClaim(claimUri, String.valueOf(claimValue));
                return;
            } else {
                setLocalClaim(claimUri, String.valueOf(claimValue));
                return;
            }
        }
        AbstractOpenJdkNashornJsObject.super.setMember(claimUri, claimValue);
    }
}
