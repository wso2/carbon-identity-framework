/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.management.cache;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCacheKey;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ActiveWebhooksCacheKey}.
 */
public class ActiveWebhooksCacheKeyTest {

    private static final String PROFILE = "WSO2";
    private static final String VERSION = "v1";
    private static final String CHANNEL = "https://schemas.identity.wso2.org/events/user";
    private static final int TENANT_ID = 1;
    private static final String DEFAULT_SCOPE = "ACTIVE";
    private static final String SUBSCRIBED_SCOPE = "SUBSCRIBED";

    @Test
    public void testGettersReturnTheConstructedValues() {

        ActiveWebhooksCacheKey key =
                new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID, SUBSCRIBED_SCOPE);

        assertEquals(key.getEventProfileName(), PROFILE);
        assertEquals(key.getEventProfileVersion(), VERSION);
        assertEquals(key.getChannelUri(), CHANNEL);
        assertEquals(key.getTenantId(), TENANT_ID);
        assertEquals(key.getScope(), SUBSCRIBED_SCOPE);
    }

    /**
     * The constructor that names no scope stands for the active webhooks, so it must produce the same key as one
     * naming that scope explicitly. Callers that predate the scope keep hitting their own entries.
     */
    @Test
    public void testScopelessConstructorDefaultsToActive() {

        ActiveWebhooksCacheKey implicit = new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID);
        ActiveWebhooksCacheKey explicit =
                new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID, DEFAULT_SCOPE);

        assertEquals(implicit.getScope(), DEFAULT_SCOPE);
        assertEquals(implicit, explicit);
        assertEquals(implicit.hashCode(), explicit.hashCode());
    }

    /**
     * A null scope is normalised rather than carried, so it cannot produce a key that equals nothing and quietly
     * misses the cache on every lookup.
     */
    @Test
    public void testNullScopeIsNormalisedToActive() {

        ActiveWebhooksCacheKey key = new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID, null);

        assertEquals(key.getScope(), DEFAULT_SCOPE);
        assertEquals(key, new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID));
    }

    /**
     * The reason the scope exists. Two lookups over the same profile, channel and tenant answer with different
     * sets of webhooks, so their entries must not be served to each other.
     */
    @Test
    public void testScopeSeparatesOtherwiseIdenticalKeys() {

        ActiveWebhooksCacheKey active = new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID);
        ActiveWebhooksCacheKey subscribed =
                new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID, SUBSCRIBED_SCOPE);

        assertNotEquals(active, subscribed);
        assertNotEquals(active.hashCode(), subscribed.hashCode());
    }

    @Test
    public void testEqualsIsReflexiveAndValueBased() {

        ActiveWebhooksCacheKey key = new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID);

        assertTrue(key.equals(key));
        assertEquals(key, new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID));
    }

    @Test
    public void testEqualsRejectsNullAndForeignTypes() {

        ActiveWebhooksCacheKey key = new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID);

        assertFalse(key.equals(null));
        assertFalse(key.equals("not a cache key"));
    }

    /**
     * Each field participates in equality. A key differing in only one of them is a different key, which is what
     * keeps one profile, version, channel or tenant from reading another's entry.
     */
    @Test
    public void testEveryFieldParticipatesInEquality() {

        ActiveWebhooksCacheKey key = new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID);

        assertNotEquals(key, new ActiveWebhooksCacheKey("CAEP", VERSION, CHANNEL, TENANT_ID));
        assertNotEquals(key, new ActiveWebhooksCacheKey(PROFILE, "v2", CHANNEL, TENANT_ID));
        assertNotEquals(key, new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL + "/login", TENANT_ID));
        assertNotEquals(key, new ActiveWebhooksCacheKey(PROFILE, VERSION, CHANNEL, TENANT_ID + 1));
    }

    /**
     * Null fields are compared rather than dereferenced, so a key built before its values were resolved does not
     * throw when the cache looks it up.
     */
    @Test
    public void testNullFieldsAreTolerated() {

        ActiveWebhooksCacheKey key = new ActiveWebhooksCacheKey(null, null, null, TENANT_ID);

        assertEquals(key, new ActiveWebhooksCacheKey(null, null, null, TENANT_ID));
        assertEquals(key.hashCode(), new ActiveWebhooksCacheKey(null, null, null, TENANT_ID).hashCode());
        assertNotEquals(key, new ActiveWebhooksCacheKey(PROFILE, null, null, TENANT_ID));
    }
}
