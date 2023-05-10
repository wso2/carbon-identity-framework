/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.extension.mgt.internal;

import org.wso2.carbon.identity.extension.mgt.ExtensionStore;
import org.wso2.carbon.identity.extension.mgt.ExtensionStoreImpl;

/**
 * Data holder for the extension manager.
 */
public class ExtensionManagerDataHolder {

    private static ExtensionManagerDataHolder instance = new ExtensionManagerDataHolder();

    private ExtensionStore extensionStore = new ExtensionStoreImpl();

    public static ExtensionManagerDataHolder getInstance() {

        return instance;
    }

    public ExtensionStore getExtensionStore() {

        return extensionStore;
    }

    public void setExtensionStore(ExtensionStore extensionStore) {

        this.extensionStore = extensionStore;
    }
}
