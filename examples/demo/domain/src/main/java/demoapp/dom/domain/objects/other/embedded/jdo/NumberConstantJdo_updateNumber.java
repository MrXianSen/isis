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
package demoapp.dom.domain.objects.other.embedded.jdo;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.PromptStyle;

import lombok.RequiredArgsConstructor;

@Profile("demo-jdo")
// tag::class[]
@Action()
@ActionLayout(
        promptStyle = PromptStyle.DIALOG_SIDEBAR
        , associateWith = "number")
@RequiredArgsConstructor
public class NumberConstantJdo_updateNumber {

    private final NumberConstantJdo numberConstantJdo;

    @MemberSupport public NumberConstantJdo act(final ComplexNumberJdo complexNumberJdo) {
        numberConstantJdo.setNumber(complexNumberJdo);
        return numberConstantJdo;
    }

    @MemberSupport public ComplexNumberJdo default0Act() {
        return numberConstantJdo.getNumber();
    }
}
// end::class[]
