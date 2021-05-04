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
package org.apache.isis.extensions.secman.model.dom.permission;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.model.dom.permission.ApplicationPermission_changing.ActionDomainEvent;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = ApplicationPermission_changing.ActionDomainEvent.class,
        associateWith = "mode")
@ActionLayout(
        sequence = "2", promptStyle = PromptStyle.INLINE
)
@RequiredArgsConstructor
public class ApplicationPermission_changing {

    public static class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationPermission_changing> {}

    private final ApplicationPermission target;

    public ApplicationPermission act() {
        target.setMode(ApplicationPermissionMode.CHANGING);
        return target;
    }

    public String disableAct() {
        return target.getMode() == ApplicationPermissionMode.CHANGING ? "Mode is already set to CHANGING": null;
    }
}
