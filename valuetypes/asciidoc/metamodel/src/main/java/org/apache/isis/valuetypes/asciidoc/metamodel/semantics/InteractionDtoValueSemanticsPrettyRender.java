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
package org.apache.isis.valuetypes.asciidoc.metamodel.semantics;

import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.metamodel.valuesemantics.InteractionDtoValueSemantics;

import lombok.NonNull;

@Component
@Named("isis.val.InteractionDtoValueSemanticsPrettyRender")
@Order(PriorityPrecedence.EARLY)
public class InteractionDtoValueSemanticsPrettyRender
extends InteractionDtoValueSemantics {

    @Override
    protected String renderXml(@NonNull final Context context, @NonNull final String xml) {
        return _XmlToHtml.toHtml(xml);
    }

}
