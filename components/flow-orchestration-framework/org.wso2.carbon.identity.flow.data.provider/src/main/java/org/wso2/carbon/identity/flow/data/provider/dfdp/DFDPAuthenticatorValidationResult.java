/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.data.provider.dfdp;

import java.util.ArrayList;
import java.util.List;

/**
 * DFDP Authenticator Validation Result.
 * Part 4: Authenticator Configuration - Validation result for authenticator configuration checks.
 */
public class DFDPAuthenticatorValidationResult {

    private boolean valid;
    private String identityProviderName;
    private String targetAuthenticator;
    private List<DFDPValidationIssue> validationIssues;

    /**
     * Constructor.
     */
    public DFDPAuthenticatorValidationResult() {
        this.validationIssues = new ArrayList<>();
        this.valid = true;
    }

    /**
     * Gets validation status.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets validation status.
     * 
     * @param valid Validation status
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Gets Identity Provider name.
     * 
     * @return Identity Provider name
     */
    public String getIdentityProviderName() {
        return identityProviderName;
    }

    /**
     * Sets Identity Provider name.
     * 
     * @param identityProviderName Identity Provider name
     */
    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
    }

    /**
     * Gets target authenticator name.
     * 
     * @return Target authenticator name
     */
    public String getTargetAuthenticator() {
        return targetAuthenticator;
    }

    /**
     * Sets target authenticator name.
     * 
     * @param targetAuthenticator Target authenticator name
     */
    public void setTargetAuthenticator(String targetAuthenticator) {
        this.targetAuthenticator = targetAuthenticator;
    }

    /**
     * Gets validation issues.
     * 
     * @return List of validation issues
     */
    public List<DFDPValidationIssue> getValidationIssues() {
        return validationIssues;
    }

    /**
     * Sets validation issues.
     * 
     * @param validationIssues List of validation issues
     */
    public void setValidationIssues(List<DFDPValidationIssue> validationIssues) {
        this.validationIssues = validationIssues;
    }

    /**
     * Adds a validation issue.
     * 
     * @param issueCode Issue code
     * @param severity Issue severity
     * @param description Issue description
     */
    public void addValidationIssue(String issueCode, String severity, String description) {
        DFDPValidationIssue issue = new DFDPValidationIssue();
        issue.setIssueCode(issueCode);
        issue.setSeverity(severity);
        issue.setDescription(description);
        this.validationIssues.add(issue);
        
        // Set overall validation to false if critical or error severity
        if ("Critical".equals(severity) || "Error".equals(severity)) {
            this.valid = false;
        }
    }

    /**
     * Gets critical issue count.
     * 
     * @return Critical issue count
     */
    public int getCriticalIssueCount() {
        return (int) validationIssues.stream().filter(issue -> "Critical".equals(issue.getSeverity())).count();
    }

    /**
     * Gets error issue count.
     * 
     * @return Error issue count
     */
    public int getErrorIssueCount() {
        return (int) validationIssues.stream().filter(issue -> "Error".equals(issue.getSeverity())).count();
    }

    /**
     * Gets warning issue count.
     * 
     * @return Warning issue count
     */
    public int getWarningIssueCount() {
        return (int) validationIssues.stream().filter(issue -> "Warning".equals(issue.getSeverity())).count();
    }

    /**
     * Gets validation summary.
     * 
     * @return Validation summary
     */
    public String getValidationSummary() {
        return String.format("Validation: %s, Total Issues: %d (Critical: %d, Error: %d, Warning: %d)",
                valid ? "PASSED" : "FAILED", validationIssues.size(),
                getCriticalIssueCount(), getErrorIssueCount(), getWarningIssueCount());
    }

    /**
     * DFDP Validation Issue.
     * Represents a single validation issue found during authenticator configuration validation.
     */
    public static class DFDPValidationIssue {

        private String issueCode;
        private String severity;
        private String description;

        /**
         * Gets issue code.
         * 
         * @return Issue code
         */
        public String getIssueCode() {
            return issueCode;
        }

        /**
         * Sets issue code.
         * 
         * @param issueCode Issue code
         */
        public void setIssueCode(String issueCode) {
            this.issueCode = issueCode;
        }

        /**
         * Gets issue severity.
         * 
         * @return Issue severity
         */
        public String getSeverity() {
            return severity;
        }

        /**
         * Sets issue severity.
         * 
         * @param severity Issue severity
         */
        public void setSeverity(String severity) {
            this.severity = severity;
        }

        /**
         * Gets issue description.
         * 
         * @return Issue description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets issue description.
         * 
         * @param description Issue description
         */
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
