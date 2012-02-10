/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.remoting.common.client.authorization;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.authorization.AuthorizationManagerAbstract;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.IdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationRequestUsability;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationRequestVisibility;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.AuthorizationResponse;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;
import org.apache.isis.runtimes.dflt.remoting.common.protocol.ObjectEncoderDecoder;

public class AuthorizationManagerProxy extends AuthorizationManagerAbstract {

    private final Map<String, Boolean> visibilityCache = new HashMap<String, Boolean>();
    private final Map<String, Boolean> usabilityCache = new HashMap<String, Boolean>();

    private final ServerFacade serverFacade;
    private final ObjectEncoderDecoder encoderDecoder;

    // ////////////////////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////////////////////

    public AuthorizationManagerProxy(final IsisConfiguration configuration, final ServerFacade serverFacade, final ObjectEncoderDecoder encoderDecoder) {
        super(configuration);
        this.serverFacade = serverFacade;
        this.encoderDecoder = encoderDecoder;
    }

    // ////////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////////

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    // ////////////////////////////////////////////////////////////////
    // API
    // ////////////////////////////////////////////////////////////////

    @Override
    public boolean isUsable(final AuthenticationSession session, final ObjectAdapter target, final Identifier identifier) {
        final IdentityData targetData = encoderDecoder.encodeIdentityData(target);

        final String idString = identifier.toIdentityString(Identifier.CLASS_MEMBERNAME_PARAMETERS);
        if (!usabilityCache.containsKey(idString)) {
            final AuthorizationResponse response = serverFacade.authorizeUsability(new AuthorizationRequestUsability(session, targetData, idString));
            final Boolean authorized = isAuthorized(response);
            usabilityCache.put(idString, authorized);
        }
        return usabilityCache.get(idString);
    }

    @Override
    public boolean isVisible(final AuthenticationSession session, final ObjectAdapter target, final Identifier identifier) {
        final IdentityData targetData = encoderDecoder.encodeIdentityData(target);

        final String idString = identifier.toIdentityString(Identifier.CLASS_MEMBERNAME_PARAMETERS);
        if (!visibilityCache.containsKey(idString)) {
            final AuthorizationRequestVisibility request = new AuthorizationRequestVisibility(session, targetData, idString);
            final AuthorizationResponse response = serverFacade.authorizeVisibility(request);
            final Boolean authorized = isAuthorized(response);
            visibilityCache.put(idString, authorized);
        }
        return visibilityCache.get(idString);
    }

    private Boolean isAuthorized(final AuthorizationResponse response) {
        // defensive coding is all (shouldn't really be null)
        return response != null ? response.isAuthorized() : false;
    }
}
