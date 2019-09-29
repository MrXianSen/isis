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
package org.apache.isis.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.facets.actions.action.command.CommandActionsConfiguration;
import org.apache.isis.metamodel.facets.actions.action.publishing.PublishActionsConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.auditing.DefaultViewConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.editing.EditingObjectsConfiguration;
import org.apache.isis.metamodel.facets.object.domainobject.publishing.PublishObjectsConfiguration;
import org.apache.isis.metamodel.facets.properties.property.command.CommandPropertiesConfiguration;
import org.apache.isis.metamodel.facets.properties.property.publishing.PublishPropertiesConfiguration;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeaturesInitConfiguration;
import org.apache.isis.metamodel.specloader.IntrospectionMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


/**
 * 
 * Configuration 'beans' with meta-data (IDE-support).
 * 
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
 * @apiNote should ultimately replace {@link IsisConfigurationLegacy}
 * 
 * @since 2.0
 *
 */
@ConfigurationProperties(ConfigurationConstants.ROOT_PREFIX)
@Data
public class IsisConfiguration {

    private final Objects objects = new Objects();
    @Data
    public static class Objects {
        private EditingObjectsConfiguration editing = EditingObjectsConfiguration.TRUE;
    }

    private final Reflector reflector = new Reflector();
    @Data
    public static class Reflector {

        private final ExplicitAnnotations explicitAnnotations = new ExplicitAnnotations();
        @Data
        public static class ExplicitAnnotations {

            /**
             * Whether or not a public method needs to be annotated with
             * @{@link org.apache.isis.applib.annotation.Action} in order to be picked up as an action in the metamodel.
             */
            private boolean action = false;
        }

        private final Facet facet = new Facet();
        @Data
        public static class Facet {

            private boolean filterVisibility = true;

            private final ActionAnnotation actionAnnotation = new ActionAnnotation();
            @Data
            public static class ActionAnnotation {
                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }

            private final CollectionAnnotation collectionAnnotation = new CollectionAnnotation();
            @Data
            public static class CollectionAnnotation {
                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }

            private final CssClass cssClass = new CssClass();
            @Data
            public static class CssClass {
                private Map<Pattern, String> patterns = new HashMap<>();
            }
            private final CssClassFa cssClassFa = new CssClassFa();
            @Data
            public static class CssClassFa {
                private Map<Pattern, String> patterns = new HashMap<>();
            }


            private final DomainObjectAnnotation domainObjectAnnotation = new DomainObjectAnnotation();
            @Data
            public static class DomainObjectAnnotation {
                private final CreatedLifecycleEvent createdLifecycleEvent = new CreatedLifecycleEvent();
                @Data
                public static class CreatedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final LoadedLifecycleEvent loadedLifecycleEvent = new LoadedLifecycleEvent();
                @Data
                public static class LoadedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final PersistingLifecycleEvent persistingLifecycleEvent = new PersistingLifecycleEvent();
                @Data
                public static class PersistingLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final PersistedLifecycleEvent persistedLifecycleEvent = new PersistedLifecycleEvent();
                @Data
                public static class PersistedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final RemovingLifecycleEvent removingLifecycleEvent = new RemovingLifecycleEvent();
                @Data
                public static class RemovingLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final UpdatedLifecycleEvent updatedLifecycleEvent = new UpdatedLifecycleEvent();
                @Data
                public static class UpdatedLifecycleEvent {
                    private boolean postForDefault = true;
                }
                private final UpdatingLifecycleEvent updatingLifecycleEvent = new UpdatingLifecycleEvent();
                @Data
                public static class UpdatingLifecycleEvent {
                    private boolean postForDefault = true;
                }
            }

            private final DomainObjectLayoutAnnotation domainObjectLayoutAnnotation = new DomainObjectLayoutAnnotation();
            @Data
            public static class DomainObjectLayoutAnnotation {
                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    private boolean postForDefault = true;
                }
                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    private boolean postForDefault = true;
                }
                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    private boolean postForDefault = true;
                }
                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    private boolean postForDefault = true;
                }
            }

            private final PropertyAnnotation propertyAnnotation = new PropertyAnnotation();
            @Data
            public static class PropertyAnnotation {
                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }
            private final ViewModelLayoutAnnotation viewModelLayoutAnnotation = new ViewModelLayoutAnnotation();
            @Data
            public static class ViewModelLayoutAnnotation {
                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    private boolean postForDefault =true;
                }
                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    private boolean postForDefault =true;
                }
                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    private boolean postForDefault =true;
                }
                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    private boolean postForDefault =true;
                }
            }
        }

        private final Facets facets = new Facets();
        @Data
        public static class Facets {
            private final ViewModelSemanticCheckingFacetFactory viewModelSemanticCheckingFacetFactory = new ViewModelSemanticCheckingFacetFactory();
            @Data
            public static class ViewModelSemanticCheckingFacetFactory {
                private boolean enable = false;
            }
            private boolean ignoreDeprecated = false;
        }

        private final Introspector introspector = new Introspector();
        @Data
        public static class Introspector {
            private boolean parallelize = true;
            private IntrospectionMode mode = IntrospectionMode.LAZY_UNLESS_PRODUCTION;
        }

        private final Validator validator = new Validator();
        @Data
        public static class Validator {

            private boolean allowDeprecated = true;
            private boolean ensureUniqueObjectTypes = true;
            private boolean checkModuleExtent = true;
            private boolean noParamsOnly = false;
            private boolean actionCollectionParameterChoices = true;
            @Deprecated
            private boolean serviceActionsOnly = true;
            @Deprecated
            private boolean mixinsOnly = true;
            private boolean explicitObjectType = false;

            private boolean jaxbViewModelNotAbstract = true;
            private boolean jaxbViewModelNotInnerClass = true;
            private boolean jaxbViewModelNoArgConstructor = false;
            private boolean jaxbViewModelReferenceTypeAdapter = true;
            private boolean jaxbViewModelDateTimeTypeAdapter = true;

            private boolean jdoqlFromClause = true;
            private boolean jdoqlVariablesClause = true;

        }
    }

    private final Services services = new Services();
    @Data
    public static class Services {

        private final ApplicationFeatures applicationFeatures = new ApplicationFeatures();
        @Data
        public static class ApplicationFeatures {
            ApplicationFeaturesInitConfiguration init = ApplicationFeaturesInitConfiguration.NOT_SPECIFIED;
        }

        private final Command command = new Command();
        @Data
        public static class Command {
            private CommandActionsConfiguration actions = CommandActionsConfiguration.NONE;
            private CommandPropertiesConfiguration properties = CommandPropertiesConfiguration.NONE;
        }

        private final Container container = new Container();
        @Data
        public static class Container {

            /**
             * Normally any queries are automatically preceded by flushing pending executions.
             *
             * <p>
             * This key allows this behaviour to be disabled.
             *
             * <p>
             *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
             *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
             *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
             *     However, that this could be circumvented by removing the call to flush().
             *     We don't want to break existing apps that might rely on this behaviour, on the
             *     other hand we want to fix the memory leak.  Adding this configuration property
             *     seems the most prudent way forward.
             * </p>
             */
            private boolean disableAutoFlush = false;

        }

        private final ExceptionRecognizerCompositeForJdoObjectStore exceptionRecognizerCompositeForJdoObjectStore = new ExceptionRecognizerCompositeForJdoObjectStore();
        @Data
        public static class ExceptionRecognizerCompositeForJdoObjectStore {
            private boolean disable = false;
        }

        private final Injector injector = new Injector();
        @Data
        public static class Injector {
            private boolean setPrefix = false;
            private boolean injectPrefix = true;
        }

        private final Publish publish = new Publish();
        @Data
        public static class Publish {
            private PublishActionsConfiguration actions = PublishActionsConfiguration.NONE;
            private PublishObjectsConfiguration objects = PublishObjectsConfiguration.NONE;
            private PublishPropertiesConfiguration properties = PublishPropertiesConfiguration.NONE;
        }

        private final Translation translation = new Translation();
        @Data
        public static class Translation {

            private final Po po = new Po();

            @Data
            public static class Po {

                TranslationService.Mode mode = TranslationService.Mode.WRITE;
            }

        }
    }

    private final Viewer viewer = new Viewer();
    @Data
    public static class Viewer {

        private final Wicket wicket = new Wicket();
        @Data
        public static class Wicket {

            private final RememberMe rememberMe = new RememberMe();
            @Data
            public static class RememberMe {
                private boolean suppress = false;
            }

            private boolean suppressSignUp = false;
            private boolean suppressPasswordReset = false;
            private boolean clearOriginalDestination = false;

            /**
             * Whether to use a modal dialog for property edits and for actions associated with properties.
             * This can be overridden on a case-by-case basis using <code>@PropertyLayout#promptStyle</code> and
             * <code>@ActionLayout#promptStyle</code>.
             *
             * This behaviour is disabled by default; the viewer will use an inline prompt in these cases, making for a smoother
             * user experience. If enabled then this reinstates the pre-1.15.0 behaviour of using a dialog prompt in all cases.
             */
            private PromptStyle promptStyle = PromptStyle.INLINE;

            private boolean showFooter = true;

            private int maxTitleLengthInTables = 12;

            private Integer maxTitleLengthInParentedTables;
            public int getMaxTitleLengthInParentedTables() {
                return maxTitleLengthInParentedTables != null ? maxTitleLengthInParentedTables : getMaxTitleLengthInTables();
            }
            /**
             * The maximum length that a title of an object will be shown when rendered in a parented table;
             * will be truncated beyond this (with ellipses to indicate the truncation).
             */
            public void setMaxTitleLengthInParentedTables(final int val) {
                maxTitleLengthInParentedTables = val;
            }

            private Integer maxTitleLengthInStandaloneTables;
            public int getMaxTitleLengthInStandaloneTables() {
                return maxTitleLengthInStandaloneTables != null ? maxTitleLengthInStandaloneTables : getMaxTitleLengthInTables();
            }
            /**
             * The maximum length that a title of an object will be shown when rendered in a standalone table;
             * will be truncated beyond this (with ellipses to indicate the truncation).
             */
            public void setMaxTitleLengthInStandaloneTables(final int val) {
                maxTitleLengthInStandaloneTables = val;
            }

            /**
             * The pattern used for rendering and parsing dates.
             *
             * <p>
             * Each Date scalar panel will use {@ #getDatePattern()} or {@linkplain #getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
            private String datePattern = "dd-MM-yyyy";
            /**
             * The pattern used for rendering and parsing date/times.
             *
             * <p>
             * Each Date scalar panel will use {@link Wicket#getDatePattern()} or {@link Wicket#getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date time picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
            private String dateTimePattern = "dd-MM-yyyy HH:mm";
            /**
             * The pattern used for rendering and parsing timestamps.
             */
            private String timestampPattern = "yyyy-MM-dd HH:mm:ss.SSS";
            /**
             * in Firefox and more recent versions of Chrome 54+, cannot copy out of disabled fields; instead we use the
             * readonly attribute (https://www.w3.org/TR/2014/REC-html5-20141028/forms.html#the-readonly-attribute)
             * This behaviour is enabled by default but can be disabled using this flag
             */
            private boolean replaceDisabledTagWithReadonlyTag = true;
            /**
             * Whether to disable a form submit button after it has been clicked, to prevent users causing an error if they
             * do a double click.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean preventDoubleClickForFormSubmit = true;
            /**
             * Whether to disable a no-arg action button after it has been clicked, to prevent users causing an error if they
             * do a double click.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean preventDoubleClickForNoArgAction = true;
            /**
             * Whether to show an indicator for a form submit button that it has been clicked.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean useIndicatorForFormSubmit = true;
            /**
             * Whether to show an indicator for a no-arg action button that it has been clicked.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean useIndicatorForNoArgAction = true;

            /**
             * Whether to redirect to a new page, even if the object being shown (after an action invocation or a property edit)
             * is the same as the previous page.
             *
             * This behaviour is disabled by default; the viewer will update the existing page if it can, making for a
             * smoother user experience. If enabled then this reinstates the pre-1.15.0 behaviour of redirecting in all cases.
             */
            private boolean redirectEvenIfSameObject = false;
        }
    }


    private final Viewers viewers = new Viewers();
    @Data
    public static class Viewers {

        private final CollectionLayout collectionLayout = new CollectionLayout();
        @Data
        public static class CollectionLayout {
            private DefaultViewConfiguration defaultView = DefaultViewConfiguration.HIDDEN;
        }

        private final Paged paged = new Paged();
        @Data
        public static class Paged {
            private int parented = 12;
            private int standalone = 25;
        }

        private final ParameterLayout parameterLayout = new ParameterLayout();
        @Data
        public static class ParameterLayout implements ConfigPropsForPropertyOrParameterLayout {
            private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            private LabelPosition label = LabelPosition.NOT_SPECIFIED;
        }
        private final PropertyLayout propertyLayout = new PropertyLayout();
        @Data
        public static class PropertyLayout implements ConfigPropsForPropertyOrParameterLayout {
            private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            private LabelPosition label = LabelPosition.NOT_SPECIFIED;
        }

    }

    @Component
    @ConfigurationPropertiesBinding
    public static class PatternsConverter implements Converter<String, Map<Pattern, String>> {
        @Override
        public Map<Pattern, String> convert(String source) {
            return toPatternMap(source);
        }

        /**
         * The pattern matches definitions like:
         * <ul>
         * <li>methodNameRegex:value</li>
         * </ul>
         *
         * <p>
         *     Used for associating cssClass and cssClassFa (font awesome icon) values to method pattern names.
         * </p>
         */
        private final static Pattern PATTERN_FOR_COLON_SEPARATED_PAIR = Pattern.compile("(?<methodRegex>[^:]+):(?<value>.+)");

        private static Map<Pattern, String> toPatternMap(String cssClassPatterns) {
            final Map<Pattern,String> valueByPattern = _Maps.newLinkedHashMap();
            if(cssClassPatterns != null) {
                final StringTokenizer regexToCssClasses = new StringTokenizer(cssClassPatterns, ConfigurationConstants.LIST_SEPARATOR);
                final Map<String,String> valueByRegex = _Maps.newLinkedHashMap();
                while (regexToCssClasses.hasMoreTokens()) {
                    String regexToCssClass = regexToCssClasses.nextToken().trim();
                    if (_Strings.isNullOrEmpty(regexToCssClass)) {
                        continue;
                    }
                    final Matcher matcher = PATTERN_FOR_COLON_SEPARATED_PAIR.matcher(regexToCssClass);
                    if(matcher.matches()) {
                        valueByRegex.put(matcher.group("methodRegex"), matcher.group("value"));
                    }
                }
                for (Map.Entry<String, String> entry : valueByRegex.entrySet()) {
                    final String regex = entry.getKey();
                    final String cssClass = entry.getValue();
                    valueByPattern.put(Pattern.compile(regex), cssClass);
                }
            }
            return valueByPattern;
        }

    }


}
