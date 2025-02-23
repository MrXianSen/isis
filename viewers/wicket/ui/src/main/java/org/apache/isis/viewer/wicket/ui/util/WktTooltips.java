/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.viewer.wicket.ui.util;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.ui.util.ExtendedPopoverConfig.PopoverBoundary;

import lombok.val;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.OpenTrigger;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@UtilityClass
public class WktTooltips {

    /**
     * To include the tooltip-css when a page is rendered.
     * @param response
     */
    public static void renderHead(final IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new CssResourceReference(WktTooltips.class, "isis-tooltips.css")));
    }

    /**
     * Adds popover behavior to the {@code target}, if at least the body is not empty/blank.
     * @param target
     * @param tooltipUiModel
     */
    public static <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable TooltipUiModel tooltipUiModel) {
        if(target ==null
                || tooltipUiModel==null
                || _Strings.isEmpty(tooltipUiModel.getBody())) {
            return target; // no body so don't render tooltip
        }

        final IModel<String> labelModel = tooltipUiModel
                .getLabel()
                .map(label->Model.of(label))
                .orElseGet(()->Model.of());
        final IModel<String> bodyModel = Model.of(tooltipUiModel.getBody());

        val tooltipBehavior = createTooltipBehavior(labelModel, bodyModel);
        Wkt.cssAppend(target, "isis-component-with-tooltip");
        target.add(tooltipBehavior);
        return target;
    }

    public static void clearTooltip(final @Nullable Component target) {
        if(target==null) {
            return;
        }
        target.getBehaviors(PopoverBehavior.class)
        .forEach(target::remove);
    }

    // -- SHORTCUTS

    public static <T extends Component> T addTooltip(
            final @Nullable T target, @Nullable final String body) {
        return addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipUiModel.ofBody(body));
    }

    public static <T extends Component> T addTooltip(
            final @Nullable T target, @Nullable final String label, @Nullable final String body) {
        return addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipUiModel.of(label, body));
    }

    // -- HELPER

    private static PopoverBehavior createTooltipBehavior(final IModel<String> label, final IModel<String> body) {
        return new PopoverBehavior(label, body, createTooltipConfig());
    }

    private static PopoverConfig createTooltipConfig() {
        return new ExtendedPopoverConfig()
        		.withBoundary(PopoverBoundary.viewport)
                .withTrigger(OpenTrigger.hover)
                .withPlacement(Placement.bottom)
                .withAnimation(true);
    }

}
