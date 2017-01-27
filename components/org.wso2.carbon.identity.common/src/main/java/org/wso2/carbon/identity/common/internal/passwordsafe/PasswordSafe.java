///*
// * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.wso2.carbon.identity.common.internal.passwordsafe;
//
//
//import org.wso2.carbon.identity.common.base.cache.BaseCache;
//
///**
// * Password safe cache.
// */
//public class PasswordSafe extends BaseCache<PasswordSafeKey, PasswordSafeEntry> {
//
//    private static final String PASSWORD_SAFE_NAME = "PasswordSafe";
//
//    private static volatile PasswordSafe instance = new PasswordSafe();
//
//    private PasswordSafe() {
//        super(PASSWORD_SAFE_NAME);
//    }
//
//    public static PasswordSafe getInstance() {
//        return instance;
//    }
//
//    public void addToCache(PasswordSafeKey key, PasswordSafeEntry entry) {
//        super.put(key, entry);
//    }
//
//    public PasswordSafeEntry getValueFromCache(PasswordSafeKey key) {
//        PasswordSafeEntry cacheEntry = super.get(key);
//        return cacheEntry;
//    }
//
//    public void clearCacheEntry(PasswordSafeKey key) {
//        super.clear(key);
//    }
//}
