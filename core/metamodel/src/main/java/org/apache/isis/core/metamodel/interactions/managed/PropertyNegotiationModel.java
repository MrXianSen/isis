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
package org.apache.isis.core.metamodel.interactions.managed;

import org.apache.isis.commons.binding.Bindable;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.binding._BindableAbstract;
import org.apache.isis.commons.internal.binding._Bindables;
import org.apache.isis.commons.internal.binding._Observables;
import org.apache.isis.commons.internal.binding._Observables.BooleanObservable;
import org.apache.isis.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed._BindingUtil.TargetFormat;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.val;

public class PropertyNegotiationModel implements ManagedValue {

    private final @NonNull BooleanObservable isCurrentValueAbsent;
    private final @NonNull _BindableAbstract<ManagedObject> proposedValue;
    private final @NonNull LazyObservable<String> validation;
    private final @NonNull _BindableAbstract<String> searchArgument;
    private final @NonNull LazyObservable<Can<ManagedObject>> choices;
    private final @NonNull ManagedProperty managedProperty;
    private Observable<String> proposedValueAsTitle;
    private Observable<String> proposedValueAsHtml;
    private Bindable<String> proposedValueAsParsableText;

    PropertyNegotiationModel(
            final ManagedProperty managedProperty) {
        this.managedProperty = managedProperty;
        val propMeta = managedProperty.getMetaModel();

        validationFeedbackActive = _Bindables.forValue(false);

        isCurrentValueAbsent = _Observables.lazyBoolean(()->
            ManagedObjects.isNullOrUnspecifiedOrEmpty(managedProperty.getPropertyValue()));

        val currentValue = managedProperty.getPropertyValue();
        val defaultValue = ManagedObjects.isNullOrUnspecifiedOrEmpty(currentValue)
            ? propMeta.getDefault(managedProperty.getOwner())
            : currentValue;

        proposedValue = _Bindables.forValue(defaultValue);
        proposedValue.setValueRefiner(EntityUtil::refetch);
        proposedValue.setValueGuard(ManagedObjects.assertInstanceOf(propMeta.getElementType()));
        proposedValue.addListener((e,o,n)->{
            invalidateChoicesAndValidation();
        });

        // has either autoComplete, choices, or none
        choices = propMeta.hasAutoComplete()
        ? _Observables.lazy(()->
            propMeta.getAutoComplete(
                    managedProperty.getOwner(),
                    getSearchArgument().getValue(),
                    InteractionInitiatedBy.USER))
        : propMeta.hasChoices()
            ? _Observables.lazy(()->
                propMeta.getChoices(managedProperty.getOwner(), InteractionInitiatedBy.USER))
            : _Observables.lazy(Can::empty);

        // if has autoComplete, then activate the search argument
        searchArgument = _Bindables.forValue(null);
        if(propMeta.hasAutoComplete()) {
            searchArgument.addListener((e,o,n)->{
                choices.invalidate();
            });
        }

        // validate this parameter, but only when validationFeedback has been activated
        validation = _Observables.lazy(()->
            isValidationFeedbackActive()
            ? managedProperty.checkValidity(getValue().getValue())
                    .map(InteractionVeto::getReason)
                    .orElse(null)
            : (String)null);
    }

    @Override
    public BooleanObservable isCurrentValueAbsent() {
        return isCurrentValueAbsent;
    }

    @Override
    public ObjectSpecification getElementType() {
        return managedProperty.getElementType();
    }

    @Override
    public Bindable<ManagedObject> getValue() {
        return proposedValue;
    }

    @Override
    public Observable<String> getValueAsTitle() {
        if(proposedValueAsTitle==null) {
            // value types should have associated renderer via value semantics
            proposedValueAsTitle = _BindingUtil
                    .bindAsFormated(TargetFormat.TITLE, managedProperty.getMetaModel(), proposedValue);
        }
        return proposedValueAsTitle;
    }

    @Override
    public Observable<String> getValueAsHtml() {
        if(proposedValueAsHtml==null) {
            // value types should have associated renderer via value semantics
            proposedValueAsHtml = _BindingUtil
                    .bindAsFormated(TargetFormat.HTML, managedProperty.getMetaModel(), proposedValue);
        }
        return proposedValueAsHtml;
    }

    @Override
    public boolean isValueAsParsableTextSupported() {
        return _BindingUtil.hasParser(managedProperty.getMetaModel());
    }

    @Override
    public Bindable<String> getValueAsParsableText() {
        if(proposedValueAsParsableText==null) {
            // value types should have associated parsers/formatters via value semantics
            // except for composite value types, which might have not
            proposedValueAsParsableText = (Bindable<String>) _BindingUtil
                    .bindAsFormated(TargetFormat.PARSABLE_TEXT, managedProperty.getMetaModel(), proposedValue);
        }
        return proposedValueAsParsableText;
    }

    @Override
    public Observable<String> getValidationMessage() {
        return validation;
    }

    @Override
    public Bindable<String> getSearchArgument() {
        return searchArgument;
    }

    @Override
    public Observable<Can<ManagedObject>> getChoices() {
        return choices;
    }

    // -- VALIDATION

    @NonNull private final _BindableAbstract<Boolean> validationFeedbackActive;

    /**
     * Whether validation feedback is activated. Activates once user attempts to 'submit' an action.
     */
    @NonNull public Observable<Boolean> getObservableValidationFeedbackActive() {
        return validationFeedbackActive;
    }

    private boolean isValidationFeedbackActive() {
        return validationFeedbackActive.getValue();
    }

    /**
     * exposed for testing
     */
    public void activateValidationFeedback() {
        validationFeedbackActive.setValue(true);
    }

    public void invalidateChoicesAndValidation() {
        choices.invalidate();
        validation.invalidate();
    }

    // -- SUBMISSION

    public void submit() {

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[PENDING MODEL] submit pending property value '%s' into owning object", getValue().getValue());
        });

        managedProperty.modifyProperty(getValue().getValue());
        isCurrentValueAbsent.invalidate();
    }


}
