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
package org.apache.isis.core.config.progmodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.inject.Vetoed;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.ObjectLifecycle;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Reflect;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramAssignableFrom;
import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public final class ProgrammingModelConstants {

    // -- TYPE VETO MARKERS (EXLUDE FROM DOMAIN)

    @Getter
    @RequiredArgsConstructor
    public enum TypeVetoMarker {
        DOMAIN_EXLCUDE(Domain.Exclude.class),
        VETO(Vetoed.class);
        private final Class<? extends Annotation> annotationType;
    }

    // -- ACCESSORS

    @Getter
    @RequiredArgsConstructor
    public enum AccessorPrefix {
        GET("get"),
        IS("is"),
        SET("set");
        private final String prefix;

        public String prefix(final @Nullable String input) {
            return input!=null
                    ? prefix + input
                    : prefix;
        }

        public boolean isPrefixOf(final @Nullable String input) {
            return input!=null
                    ? input.startsWith(prefix)
                    : false;
        }

        public static boolean isCandidateGetterName(final @Nullable String name) {
            return GET.isPrefixOf(name)
                    || IS.isPrefixOf(name);
        }

        public static boolean isBooleanGetter(final Method method) {
            return IS.isPrefixOf(method.getName())
                    && method.getParameterCount() == 0
                    && !Modifier.isStatic(method.getModifiers())
                    && (method.getReturnType() == boolean.class
                        || method.getReturnType() == Boolean.class);
        }

        public static boolean isNonBooleanGetter(final Method method, final Predicate<Class<?>> typeFilter) {
            return GET.isPrefixOf(method.getName())
                    && method.getParameterCount() == 0
                    && !Modifier.isStatic(method.getModifiers())
                    && typeFilter.test(method.getReturnType());
        }

        public static boolean isNonBooleanGetter(final Method method, final Class<?> expectedType) {
            return isNonBooleanGetter(method, type->
                expectedType.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(type)));
        }

        public static boolean isGetter(final Method method) {
            return isBooleanGetter(method)
                    || isNonBooleanGetter(method, type->type != void.class);
        }
    }

    // -- CANONICAL DATE-TIME PARSING/FORMATTING

    @RequiredArgsConstructor
    public enum DateTimeFormat {
        /**
         * Parser-format: {@literal "yyyy-MM-dd HH:mm:ss[.SSS][' '][XXX][x]"}<br>
         * Render-format: {@literal "yyyy-MM-dd HH:mm:ss.SSS XXX"}<br>
         * Examples:<br>
         * <ul>
         * <li>"2022-01-31 14:04:33.017 -03:30" (full form)</li>
         * <li>"2022-01-31 14:04:33 -03" (no millis, no offset minutes)</li>
         * <li>"2022-01-31 14:04:33 Z" (no millis, no offset = UTC)</li>
         * </ul>
         * <p>
         * Used eg. with {@code InteractAs(frozenDateTime=...)} annotation.
         */
        CANONICAL(
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd HH:mm:ss[.SSS][' '][XXX][x]")
                        .parseLenient()
                        .parseCaseInsensitive()
                        .toFormatter(Locale.ROOT),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS XXX", Locale.ROOT));

        public OffsetDateTime parseDateTime(final String dateTimeLiteral) {
            return OffsetDateTime.parse(dateTimeLiteral, parser);
        }

        public String formatDateTime(final OffsetDateTime dateTime) {
            return formatter.format(dateTime);
        }

        // -- HELPER

        private final DateTimeFormatter parser;
        private final DateTimeFormatter formatter;

    }


    // -- MIXIN CONSTRUCTION

    public enum MixinConstructor {
        /**
         * Assuming, mixins do have a public single argument constructor,
         * that receive an instance of the mixee's type.
         */
        PUBLIC_SINGLE_ARG_RECEIVING_MIXEE;

        // while this enum only has a single value, we just provide a (quasi) static method here
        public <T> Constructor<T> getConstructorElseFail(
                final @NonNull Class<T> mixinClass,
                final @NonNull Class<?> mixeeClass) {
            return _Casts.uncheckedCast(_Reflect
                        .getPublicConstructors(mixinClass)
                        .filter(paramCount(1).and(paramAssignableFrom(0, mixeeClass)))
                    .getSingleton()
                    .orElseThrow(()->_Exceptions.illegalArgument(
                            "Failed to locate constructor in '%s' to instantiate,"
                            + "when using type '%s' as first argument",
                            mixinClass.getName(), mixinClass.getName())));
        }

        // while this enum only has a single value, we just provide a (quasi) static method here
        public <T> Can<Constructor<T>> getConstructors(final Class<T> candidateMixinType) {
            val mixinContructors = _Reflect
                    .getPublicConstructors(candidateMixinType)
                    .filter(paramCount(1));
            return _Casts.uncheckedCast(mixinContructors);
        }
    }

    // -- LIFECYCLE CALLBACKS

    @Getter
    public enum CallbackMethod {
        CREATED("created"),
        LOADED("loaded"),
        PERSISTED("persisted", "saved"),
        PERSISTING("persisting", "saving"),
        REMOVING("removing", "deleting"),
        UPDATED("updated"),
        UPDATING("updating");
        CallbackMethod(final String ...methodNames) {
            this.methodNames = Can.of(methodNames);
        }
        private final Can<String> methodNames;
    }

    // -- TYPE CATEGORIES

    @Getter
    public enum ReturnTypeCategory {
        VOID(void.class),
        BOOLEAN(boolean.class),
        STRING(String.class),
        TRANSLATABLE(String.class, TranslatableString.class);
        ReturnTypeCategory(final Class<?> ...returnTypes) {
            this.returnTypes = Can.of(returnTypes);
        }
        private final Can<Class<?>> returnTypes;

        public static Can<Class<?>> nonScalar(final @NonNull Class<?> elementType) {
            if(VOID.returnTypes.contains(elementType)) {
                return Can.empty();
            }
            return Can.<Class<?>>of(
                Can.class,
                Collection.class,
                Array.newInstance(elementType, 0).getClass());
        }
    }

    // -- PARAMETER SUPPORT

    @RequiredArgsConstructor
    public static enum ReturnTypePattern {
        SCALAR(Can::ofSingleton),
        NON_SCALAR(ReturnTypeCategory::nonScalar),
        TEXT(__->ReturnTypeCategory.TRANSLATABLE.getReturnTypes()),
        BOOLEAN(__->ReturnTypeCategory.BOOLEAN.getReturnTypes());
        final Function<Class<?>, Can<Class<?>>> matchingTypesForElementType;
        public Can<Class<?>> matchingTypes(final @NonNull Class<?> elementType) {
            return matchingTypesForElementType.apply(elementType);
        }
    }

    // -- OBJECT SUPPORT

    @Getter
    public enum ObjectSupportMethod {
        /** for batch disabling all members */
        DISABLED(ReturnTypeCategory.TRANSLATABLE, "disabled"),

        /** for batch hiding all members */
        HIDDEN(ReturnTypeCategory.BOOLEAN, "hidden"),

        TITLE(ReturnTypeCategory.TRANSLATABLE, "title"),
        CSS_CLASS(ReturnTypeCategory.STRING, "cssClass"),
        ICON_NAME(ReturnTypeCategory.STRING, "iconName"),
        LAYOUT(ReturnTypeCategory.STRING, "layout"),

        /** as a fallback in the absence of other title providers */
        TO_STRING(ReturnTypeCategory.STRING, "toString"),
        ;
        ObjectSupportMethod(
                final ReturnTypeCategory returnTypeCategory,
                final String ...methodNames) {
            this.returnTypeCategory = returnTypeCategory;
            this.methodNames = Can.of(methodNames);
        }
        private final ReturnTypeCategory returnTypeCategory;
        private final Can<String> methodNames;
    }

    // -- MEMBER SUPPORT PREFIXES

    @Getter
    public static enum MemberSupportPrefix {
        DEFAULT(ReturnTypePattern.SCALAR, "default"),
        CHOICES(ReturnTypePattern.NON_SCALAR, "choices"),
        AUTO_COMPLETE(ReturnTypePattern.NON_SCALAR, "autoComplete"),
        HIDE(ReturnTypePattern.BOOLEAN, "hide"),
        DISABLE(ReturnTypePattern.TEXT, "disable"),
        VALIDATE(ReturnTypePattern.TEXT, "validate"),
        NAMED(ReturnTypePattern.TEXT, "named"), // imperative naming
        DESCRIBED(ReturnTypePattern.TEXT, "described"); // imperative naming
        MemberSupportPrefix(
                final ReturnTypePattern parameterSearchReturnType,
                final String ...methodNamePrefixes) {
            this.supportMethodReturnType = parameterSearchReturnType;
            this.methodNamePrefixes = Can.of(methodNamePrefixes);
        }
        private final ReturnTypePattern supportMethodReturnType;
        private final Can<String> methodNamePrefixes;
    }

    // -- CONFLICTING MARKER ANNOTATIONS

    @Getter
    @RequiredArgsConstructor
    public static enum ConflictingAnnotations {
        OBJECT_SUPPORT(Can.of(ObjectLifecycle.class, MemberSupport.class)),
        OBJECT_LIFECYCLE(Can.of(ObjectSupport.class, MemberSupport.class)),
        MEMBER_SUPPORT(Can.of(ObjectSupport.class, ObjectLifecycle.class));
        final Can<Class<? extends Annotation>> prohibits;
    }

    // -- SUPPORTING METHOD NAMING CONVENTION

    public static enum ActionSupportNaming {
        /** eg. hideAct() */
        PREFIXED_ACTION_NAME {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin) {
                return prefix + _Strings.capitalize(actionMethod.getName());
            }
        },
        /** eg. hide() */
        PREFIX_ONLY {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin) {
                return isMixin
                        // prefix-only notation is restricted to mixins
                        ? prefix
                        : null;
            }
        };
        abstract @Nullable String nameFor(Method actionMethod, String prefix, boolean isMixin);
        public static Can<String> namesFor(final Method actionMethod, final String prefix, final boolean isMixin) {
            return Stream.of(ActionSupportNaming.values())
                    .map(naming->naming.nameFor(actionMethod, prefix, isMixin))
                    .collect(Can.toCan());
        }
    }

    public static enum ParameterSupportNaming {
        /** eg. hide2Act(..) */
        PREFIX_PARAM_INDEX_ACTION_NAME {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin, final int paramNum) {
                return prefix + paramNum + _Strings.capitalize(actionMethod.getName());
            }
        },
        /** eg. hideEmail() .. where email is the referenced parameter's name */
        PREFIXED_PARAM_NAME {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin, final int paramNum) {
                return isMixin
                        // no-action-name-reference notation is restricted to mixins
                        ? prefix + _Strings.capitalize(actionMethod.getParameters()[paramNum].getName())
                        : null;
            }
        };
        abstract @Nullable String nameFor(Method actionMethod, String prefix, boolean isMixin, int paramNum);
        public static Can<IntFunction<String>> namesFor(final Method actionMethod, final String prefix, final boolean isMixin) {
            return Stream.of(ParameterSupportNaming.values())
                    .<IntFunction<String>>map(naming->(paramNum->naming.nameFor(actionMethod, prefix, isMixin, paramNum)))
                    .collect(Can.toCan());
        }
    }

    /** deals with <i>fields</i>, <i>getters</i> and <i>actions</i> */
    public static enum MemberSupportNaming {
        /** eg. hideProp() */
        PREFIXED_MEMBER_NAME {
            @Override @Nullable
            String nameFor(final Member member, final String prefix, final boolean isMixin) {
                return prefix + getCapitalizedMemberName(member);
            }
        },
        /** eg. hide() */
        PREFIX_ONLY {
            @Override @Nullable
            String nameFor(final Member member, final String prefix, final boolean isMixin) {
                return isMixin
                        // prefix-only notation is restricted to mixins
                        ? prefix
                        : null;
            }
        };
        abstract @Nullable String nameFor(Member member, String prefix, boolean isMixin);
        public static Can<String> namesFor(final Member member, final String prefix, final boolean isMixin) {
            return Stream.of(MemberSupportNaming.values())
                    .map(naming->naming.nameFor(member, prefix, isMixin))
                    .collect(Can.toCan());
        }
    }

    //maybe consolidate all MM validation raisers here, if we can find a common method signature for that
    @RequiredArgsConstructor
    public static enum Validation {
        CONFLICTING_TITLE_STRATEGIES(
                "${type} has title() method with @Title annotation, which is not allowed; "
                + "consider either removing the @Title annotation or renaming the method"),
        ORPHANED_METHOD("${type}#${member}: is public, but orphaned (was not picked up by the framework); "
                + "reporting orphans, because the class is setup for member introspection, "
                + "without enforcing annotations"),
        UNSATISFIED_DOMAIN_INCLUDE_SEMANTICS("${type}#${member}: "
                + "has synthesized (effective) annotation @Domain.Include, "
                + "is assumed to represent or support a property, collection or action."),
        VIEWMODEL_MISSING_DESERIALIZING_CONSTRUCTOR(
                "${type}: ViewModel contract violation: missing single (String) arg constructor "
                + "(for de-serialization from memento string).");
        ;
        private final String template;
        public String getMessage(final Identifier featureIdentifier) {
            return getMessageForTypeAndMemberId(
                    featureIdentifier.getLogicalType().getClassName(),
                    featureIdentifier.getMemberLogicalName());
        }
        public String getMessageForType(final String type) {
            return getMessage(Map.of(
                    "type", type));
        }
        public String getMessageForTypeAndMemberId(final String type, final String memberId) {
            return getMessage(Map.of(
                    "type", type,
                    "member", memberId));
        }
        public String getMessage(final Map<String, String> templateVars) {
            return processMessageTemplate(template, templateVars);
        }
    }

    public static enum ViewmodelConstructor {
        SINGLE_STRING_ARG {

            @Override
            public <T> Optional<Constructor<T>> get(final Class<T> cls) {
                // heap-pollution: only produces stack-traces when cls violates viewmodel contract,
                // which is covered by mm validation
                return Result.of(()->
                        cls.getDeclaredConstructor(new Class<?>[]{String.class}))
                        .getValue();
            }

        };
        public abstract <T> Optional<Constructor<T>> get(Class<T> correspondingClass);

    }

    // -- HELPER

    private static String getCapitalizedMemberName(final Member member) {
        if(member instanceof Method) {
            val method = (Method)member;
            val methodName = method.getName();
            if(method.getParameterCount()>0
                    || method.getReturnType().equals(void.class)
                    || !AccessorPrefix.isCandidateGetterName(methodName)) {
                // definitely an action not a getter
                return _Strings.capitalize(methodName);
            }
            // must be a getter
            return _Strings.capitalize(_Strings.asPrefixDropped(methodName));
        }
        // must be a field then
        return _Strings.capitalize(member.getName());
    }

    private static String processMessageTemplate(
            final String template,
            final Map<String, String> templateVars) {

        val templateRef = _Refs.stringRef(template);
        templateVars.forEach((k, v)->templateRef.update(str->str.replace("${" + k + "}", v)));
        return templateRef.getValue();
    }


}
