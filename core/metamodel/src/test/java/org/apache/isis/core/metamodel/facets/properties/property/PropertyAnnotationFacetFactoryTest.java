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
package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.hamcrest.MatcherAssert;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Snapshot;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.entitychangepublishing.EntityPropertyChangePublishingPolicyFacet;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.snapshot.SnapshotExcludeFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class PropertyAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    PropertyAnnotationFacetFactory facetFactory;
    Method propertyMethod;

    @Mock ObjectSpecification mockTypeSpec;
    @Mock ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoader).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));

            allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
            will(returnValue(null));

        }});
    }

    private static void processModify(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processModify(processMethodContext, propertyIfAny);
    }

    private static void processHidden(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processHidden(processMethodContext, propertyIfAny);
    }

    private static void processOptional(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processOptional(processMethodContext, propertyIfAny);
    }

    private static void processRegEx(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processRegEx(processMethodContext, propertyIfAny);
    }

    private static void processEditing(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processEditing(processMethodContext, propertyIfAny);
    }

    private static void processMaxLength(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processMaxLength(processMethodContext, propertyIfAny);
    }

    private static void processMustSatisfy(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processMustSatisfy(processMethodContext, propertyIfAny);
    }

    private static void processSnapshot(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processSnapshot(processMethodContext, propertyIfAny);
    }

    private static void processEntityPropertyChangePublishing(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processEntityPropertyChangePublishing(processMethodContext, propertyIfAny);
    }


    @Before
    public void setUp() throws Exception {
        facetFactory = new PropertyAnnotationFacetFactory(metaModelContext);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class Modify extends PropertyAnnotationFacetFactoryTest {

        private void addGetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyOrCollectionAccessorFacetAbstract(mockOnType, holder) {
                @Override
                public Object getProperty(
                        final ManagedObject inObject,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                    return null;
                }
            });
        }

        private void addSetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertySetterFacetAbstract(holder) {
                @Override
                public ManagedObject setProperty(
                        final OneToOneAssociation owningAssociation,
                        final ManagedObject inObject,
                        final ManagedObject value,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                    return inObject;
                }
            });
        }

        private void addClearFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyClearFacetAbstract(holder) {
                @Override
                public ManagedObject clearProperty(
                        final OneToOneAssociation owningProperty,
                        final ManagedObject targetAdapter,
                        final InteractionInitiatedBy interactionInitiatedBy) {
                    return targetAdapter;
                }
            });
        }


        @Test
        public void withDeprecatedPostsPropertyChangedEvent_andGetterFacet_andSetterFacet() {

            class Customer {
                class NamedChangedDomainEvent extends PropertyDomainEvent<Customer, String> {}
                @Property(domainEvent = NamedChangedDomainEvent.class)
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());
            context.checking(new Expectations() {{

                allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
                will(returnValue(null));
            }});

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final PropertyDomainEventFacet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetDefault = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            assertThat(domainEventFacetDefault.getEventType(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            assertNotNull(setterFacet);
            assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation,
                    "unexpected facet: " + setterFacet);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            assertNotNull(clearFacet);
            assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));
        }


        @Test
        public void withPropertyInteractionEvent() {

            class Customer {
                class NamedChangedDomainEvent extends PropertyDomainEvent<Customer, String> {}
                @Property(domainEvent = NamedChangedDomainEvent.class)
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            context.checking(new Expectations() {{
                allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
                will(returnValue(null));
            }});

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetImpl = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            assertNotNull(setterFacet);
            assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation,
                    "unexpected facet: " + setterFacet);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            assertNotNull(clearFacet);
            assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));
        }

        @Test
        public void withPropertyDomainEvent() {

            class Customer {
                class NamedChangedDomainEvent extends PropertyDomainEvent<Customer, String> {
                }
                @Property(domainEvent= NamedChangedDomainEvent.class)
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            context.checking(new Expectations() {{
                allowing(mockTypeSpec).getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
                will(returnValue(null));
            }});
            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetImpl = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            MatcherAssert.assertThat(domainEventFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            assertNotNull(setterFacet);
            assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation,
                    "unexpected facet: " + setterFacet);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            assertNotNull(clearFacet);
            assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(Customer.NamedChangedDomainEvent.class));
        }

        @Test
        public void withDefaultEvent() {

            class Customer {
                @Getter @Setter private String name;
            }

            // given
            assertTrue(metaModelContext.getConfiguration()
                    .getApplib().getAnnotation().getDomainObject().getCreatedLifecycleEvent().isPostForDefault());

            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processModify(facetFactory, processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            assertNotNull(domainEventFacet);
            assertTrue(domainEventFacet instanceof PropertyDomainEventFacetDefault);
            final PropertyDomainEventFacetDefault domainEventFacetImpl = (PropertyDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), IsisMatchers.classEqualTo(PropertyDomainEvent.Default.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            assertNotNull(setterFacet);
            assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromDefault,
                    "unexpected facet: " + setterFacet);
            final PropertySetterFacetForDomainEventFromDefault setterFacetImpl = (PropertySetterFacetForDomainEventFromDefault) setterFacet;
            assertThat(setterFacetImpl.value(), IsisMatchers.classEqualTo(PropertyDomainEvent.Default.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            assertNotNull(clearFacet);
            assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromDefault);
            final PropertyClearFacetForDomainEventFromDefault clearFacetImpl = (PropertyClearFacetForDomainEventFromDefault) clearFacet;
            assertThat(clearFacetImpl.value(), IsisMatchers.classEqualTo(PropertyDomainEvent.Default.class));
        }
    }

    public static class Hidden extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            @SuppressWarnings("unused")
            class Customer {
                @Property(hidden = Where.REFERENCES_PARENT)
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processHidden(facetFactory, processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            assertNotNull(hiddenFacet);
            assertTrue(hiddenFacet instanceof HiddenFacetForPropertyAnnotation);
            final HiddenFacetForPropertyAnnotation hiddenFacetImpl = (HiddenFacetForPropertyAnnotation) hiddenFacet;
            assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));

            final Facet hiddenFacetForProp = facetedMethod.getFacet(HiddenFacet.class);
            assertNotNull(hiddenFacetForProp);
            assertTrue(hiddenFacet == hiddenFacetForProp);
        }

    }

    @SuppressWarnings("unused")
    public static class Editing extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotationOnGetter() {

            class Customer {
                @Property(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the name property"
                        )
                public String getName() { return null; }
                public void setName(final String name) {}
            }

            assertDisabledFacetOn(findMethod(Customer.class, "getName"),
                    "you cannot edit the name property");
        }

        @Test
        public void withAnnotationOnField() {

            class Customer {
                @Property(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the name property"
                        )
                @Getter @Setter
                private String name;
            }

            assertDisabledFacetOn(findMethod(Customer.class, "getName"),
                    "you cannot edit the name property");
        }


        @Test
        public void withAnnotationOnBooleanGetter() {

            class Customer {
                @Property(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the subscribed property"
                        )
                public boolean isSubscribed() { return true; }
                public void setSubscribed(final boolean b) {}
            }

            assertDisabledFacetOn(findMethod(Customer.class, "isSubscribed"),
                    "you cannot edit the subscribed property");
        }

        @Test
        public void withAnnotationOnBooleanField() {

            class Customer {
                @Property(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the subscribed property"
                        )
                @Getter @Setter
                private boolean subscribed;
            }

            assertDisabledFacetOn(findMethod(Customer.class, "isSubscribed"),
                    "you cannot edit the subscribed property");
        }

        // -- SPECIAL SCENARIO ISIS-2963

        static interface PrimitiveBooleanHolder {
            @Property(
                    editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                    editingDisabledReason = "a")
            boolean isReadWriteProperty();
            void setReadWriteProperty(boolean c);
        }

        static class PrimitiveBooleanEntity implements PrimitiveBooleanHolder {
            @Property(
                    editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                    editingDisabledReason = "b")
            @Getter @Setter
            private boolean readWriteProperty;
        }

        @Test //FIXME[ISIS-2963] test fails - no facet is generated
        public void isis2963() {
            assertDisabledFacetOn(findMethod(PrimitiveBooleanEntity.class, "isReadWriteProperty"),
                    "b");
        }

        // -- HELPER

        private void assertDisabledFacetOn(final Method getter, final String expectedDisabledReason) {

            // given
            final Class<?> cls = getter.getDeclaringClass();
            propertyMethod = getter;

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processEditing(facetFactory, processMethodContext);

            // then
            val disabledFacet = facetedMethod.getFacet(DisabledFacet.class);
            assertNotNull(disabledFacet);
            assertTrue(disabledFacet instanceof DisabledFacetForPropertyAnnotation);
            val disabledFacet2 = (DisabledFacetForPropertyAnnotation) disabledFacet;
            assertThat(disabledFacet.where(), is(Where.EVERYWHERE));
            assertThat(disabledFacet2.disabledReason(null), is(expectedDisabledReason));
        }

    }

    public static class MaxLength extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        maxLength = 30
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processMaxLength(facetFactory, processMethodContext);

            // then
            final MaxLengthFacet maxLengthFacet = facetedMethod.getFacet(MaxLengthFacet.class);
            assertNotNull(maxLengthFacet);
            assertTrue(maxLengthFacet instanceof MaxLengthFacetForPropertyAnnotation);
            assertThat(maxLengthFacet.value(), is(30));
        }
    }

    public static class MustSatisfy extends PropertyAnnotationFacetFactoryTest {

        public static class NotTooHot implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }

        public static class NotTooCold implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }


        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        mustSatisfy = {NotTooHot.class, NotTooCold.class}
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processMustSatisfy(facetFactory, processMethodContext);

            // then
            final MustSatisfySpecificationFacet mustSatisfySpecificationFacet = facetedMethod.getFacet(MustSatisfySpecificationFacet.class);
            assertNotNull(mustSatisfySpecificationFacet);
            assertTrue(mustSatisfySpecificationFacet instanceof MustSatisfySpecificationFacetForPropertyAnnotation);
            final MustSatisfySpecificationFacetForPropertyAnnotation mustSatisfySpecificationFacetImpl = (MustSatisfySpecificationFacetForPropertyAnnotation) mustSatisfySpecificationFacet;
            val specifications = mustSatisfySpecificationFacetImpl.getSpecifications();
            assertThat(specifications.size(), is(2));

            assertTrue(specifications.getElseFail(0) instanceof NotTooHot);
            assertTrue(specifications.getElseFail(1) instanceof NotTooCold);
        }

    }

    public static class EntityPropertyChangePublishingPolicy extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void exclusion() {

            class Customer {
                @Property(entityChangePublishing = Publishing.DISABLED)
                @Getter @Setter private String name;
            }

            // given
            val cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processEntityPropertyChangePublishing(facetFactory, processMethodContext);

            // then
            val changePolicyFacet = facetedMethod.getFacet(EntityPropertyChangePublishingPolicyFacet.class);
            assertNotNull(changePolicyFacet);
            assertTrue(changePolicyFacet.isPublishingVetoed());
            assertFalse(changePolicyFacet.isPublishingAllowed());
        }

        @Test
        public void whenDefault() {

            class Customer {
                @Property
                @Getter @Setter private String name;
            }

            // given
            val cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processEntityPropertyChangePublishing(facetFactory, processMethodContext);

            // then
            val changePolicyFacet = facetedMethod.getFacet(EntityPropertyChangePublishingPolicyFacet.class);
            assertNull(changePolicyFacet);
        }

    }

    public static class SnapshotExcluded extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(snapshot = Snapshot.EXCLUDED)
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processSnapshot(facetFactory, processMethodContext);

            // then
            final SnapshotExcludeFacet snapshotExcludeFacet = facetedMethod.getFacet(SnapshotExcludeFacet.class);
            assertNotNull(snapshotExcludeFacet);
            assertTrue(snapshotExcludeFacet instanceof SnapshotExcludeFacetForPropertyAnnotation);
        }

    }

    public static class Mandatory extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void whenOptionalityIsTrue() {

            class Customer {
                @Property(
                        optionality = Optionality.OPTIONAL
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            assertNotNull(mandatoryFacet);
            assertTrue(mandatoryFacet instanceof MandatoryFacetForPropertyAnnotation.Optional);
        }

        @Test
        public void whenOptionalityIsFalse() {

            class Customer {
                @Property(
                        optionality = Optionality.MANDATORY
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            assertNotNull(mandatoryFacet);
            assertTrue(mandatoryFacet instanceof MandatoryFacetForPropertyAnnotation.Required);
        }

        @Test
        public void whenOptionalityIsDefault() {

            class Customer {
                @Property(
                        optionality = Optionality.DEFAULT
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            assertNull(mandatoryFacet);
        }

        @Test
        public void whenNone() {

            class Customer {
                @Property(
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processOptional(facetFactory, processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            assertNull(mandatoryFacet);
        }

    }
    public static class RegEx extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void whenHasAnnotation() {

            class Customer {
                @Property(
                        regexPattern = "[123].*",
                        regexPatternFlags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            assertNotNull(regExFacet);
            assertTrue(regExFacet instanceof RegExFacetForPropertyAnnotation);
            assertThat(regExFacet.patternFlags(), is(10));
            assertThat(regExFacet.regexp(), is("[123].*"));
        }

        @Test
        public void whenNone() {

            class Customer {
                @Property(
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            assertNull(regExFacet);
        }

        @Test
        public void whenEmptyString() {

            class Customer {
                @Property(
                        regexPattern = ""
                        )
                @Getter @Setter private String name;
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            assertNull(regExFacet);
        }

        @Test
        public void whenNotAnnotatedOnStringProperty() {

            class Customer {
                @Property(
                        regexPattern = "[abc].*"
                        )
                public int getName() {return 0; }
                @SuppressWarnings("unused") public void setName(final int name) { }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            val processMethodContext = ProcessMethodContext
                    .forTesting(cls, null,
                    propertyMethod, mockMethodRemover, facetedMethod);
            processRegEx(facetFactory, processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            assertNull(regExFacet);
        }

    }

}
