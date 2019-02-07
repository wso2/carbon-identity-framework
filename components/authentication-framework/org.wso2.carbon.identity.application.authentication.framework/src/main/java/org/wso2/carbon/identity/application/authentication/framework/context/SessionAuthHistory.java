/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.context;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds the history of what happens on the authentication session, while performing the authentication.
 */
public class SessionAuthHistory implements Serializable {

    private static final long serialVersionUID = -4403150321922576472L;
    
    /**
     * ACR value selected for the authentication session, if any.
     */
    private String selectedAcrValue;

    /**
     * History of what happens on the authentication flow
     */
    private LinkedList<AuthHistory> authenticationStepHistory = new LinkedList<>();

    public String getSelectedAcrValue() {
        return selectedAcrValue;
    }

    public void setSelectedAcrValue(String selectedAcrValue) {
        this.selectedAcrValue = selectedAcrValue;
    }

    /**
     * Pushes an entry to the history stack.
     *
     * @param historyElement
     */
    public void push(AuthHistory historyElement) {
        authenticationStepHistory.addLast(historyElement);
    }

    /**
     * Undo/Remove an entry to the history stack.
     * This can be done if the current authenticator decides it is FALLBACK.
     *
     * @return The last undone history element.
     */
    public AuthHistory undo() {
        return authenticationStepHistory.removeLast();
    }

    /**
     * Undo/Remove entries upto and including the supplied entry from the history stack.
     * This can be done if the current authenticator decides it is FALLBACK.
     *
     * @return The last undone history element. Null if there is no matching entry.
     */
    public AuthHistory undoUpto(AuthHistory historyElement) {
        LinkedList<AuthHistory> modifyingHistory = new LinkedList<>();
        AuthHistory matchedEntry = null;
        for (AuthHistory historyEntry : authenticationStepHistory) {
            if (historyEntry.equals(historyElement)) {
                matchedEntry = historyEntry;
                break;
            }
            modifyingHistory.add(historyEntry);
        }

        if (matchedEntry != null) {
            authenticationStepHistory = modifyingHistory;
        }
        return matchedEntry;
    }

    /**
     * Returns the current snapshot of authentication history. The resultant list is not modifiable.
     *
     * @return Unmodifiable list of history elements.
     */
    public List<AuthHistory> getHistory() {
        return Collections.unmodifiableList(authenticationStepHistory);
    }

    /**
     * Resets the histories with the given authentication history.
     *
     * @param authHistories
     */
    public void resetHistory(List<AuthHistory> authHistories) {
        authenticationStepHistory = new LinkedList<>(authHistories);
    }
}
