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
package org.apache.isis.applib.services.appfeatui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

/**
 * @since 2.x  {@index}
 */
@DomainObject(
        logicalTypeName = ApplicationTypeAction.LOGICAL_TYPE_NAME
)
@DomainObjectLayout(
        paged = 100
)
public class ApplicationTypeAction extends ApplicationTypeMember {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE_FEAT + ".ApplicationTypeAction";

    public static abstract class PropertyDomainEvent<T> extends ApplicationTypeMember.PropertyDomainEvent<ApplicationTypeAction, T> {}

    // -- constructors
    public ApplicationTypeAction() { }
    public ApplicationTypeAction(final ApplicationFeatureId featureId) {
        super(featureId);
    }
    public ApplicationTypeAction(final String memento) {
        super(memento);
    }


    // -- returnTypeName (property)

    @ApplicationFeatureViewModel.TypeSimpleName
    @Property(
            domainEvent = ReturnType.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "dataType",
            sequence = "2.6"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReturnType {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @PropertyLayout(
            fieldSetId = "dataType", // TODO: shouldn't be necessary??
            sequence = "2.6"
    )
    @ReturnType
    public String getReturnType() {
        return getFeature().getActionReturnType()
                .map(Class::getSimpleName)
                .orElse("<none>");
    }


    // -- actionSemantics (property)

    @Property(
            domainEvent = ActionSemantics.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "detail",
            sequence = "2.8"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ActionSemantics {

        class DomainEvent extends PropertyDomainEvent<SemanticsOf> {}
    }

    @ActionSemantics
    public SemanticsOf getActionSemantics() {
        return getFeature().getActionSemantics()
                .orElse(null);
    }

}
