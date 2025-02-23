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
package org.apache.isis.viewer.wicket.ui.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;

import lombok.val;

/**
 * Immutable tree, hooks into the ScalarPanelTextField without actually using its text field.
 */
public class ParentedTreePanel
extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    public ParentedTreePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected MarkupContainer createComponentForRegular() {
        return createTreeComponent(getScalarTypeContainer(), ID_SCALAR_IF_REGULAR);
    }

    @Override
    protected MarkupContainer createComponentForCompact() {
        return createTreeComponent(getScalarTypeContainer(), ID_SCALAR_IF_COMPACT);
    }

    @Override
    protected InlinePromptConfig getInlinePromptConfig() {
        return InlinePromptConfig.notSupported();
    }

    @Override
    protected Component getValidationFeedbackReceiver() {
        return null;
    }

    // -- HELPER

    private MarkupContainer createTreeComponent(final MarkupContainer parent, final String id) {
        val scalarModel = scalarModel();
        val tree = IsisToWicketTreeAdapter.adapt(id, scalarModel);
        parent.add(tree);
        // adds the tree-theme behavior to the tree's parent
        parent.add(getTreeThemeProvider().treeThemeFor(scalarModel));
        return (MarkupContainer) tree;
    }


}
