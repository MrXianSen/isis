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
package org.apache.isis.viewer.wicket.ui.components.scalars.markup;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

public class StandaloneMarkupPanel
extends PanelAbstract<ManagedObject, ValueModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_STANDALONE_VALUE = "standaloneValue";

    public StandaloneMarkupPanel(
            final String id,
            final ValueModel valueModel,
            final MarkupComponentFactory<ValueModel> markupComponentFactory) {

        super(id, valueModel);
        val markupComponent = markupComponentFactory
                .newMarkupComponent(ID_STANDALONE_VALUE, getModel());
        add(markupComponent);
    }

}
