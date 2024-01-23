/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.role.mgt.ui;


import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bean class used to render pagination
 */
public class PaginatedNamesBean implements Pageable {
    private FlaggedName[] names = new FlaggedName[0];
    private int numberOfPages;

    public FlaggedName[] getNames() {
        return Arrays.copyOf(names, names.length);
    }

    public String[] getNamesAsString() {
        List<String> list = new ArrayList<String>();

        for (FlaggedName name : names) {

            list.add(name.getItemName());
        }
        return list.toArray(new String[list.size()]);
    }

    public void setNames(FlaggedName[] names) {
        this.names = Arrays.copyOf(names, names.length);
    }

    @Override
    public int getNumberOfPages() {
        return numberOfPages;
    }

    @Override
    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Override
    public <T extends Object> void set(List<T> t) {
        this.names = t.toArray(new FlaggedName[t.size()]);
    }


}
