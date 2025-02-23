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
package org.apache.isis.core.metamodel.interactions;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Implemented by some of the {@link InteractionContext} subclasses, making it
 * easier for facets to process multiple at the same time.
 *
 * <p>
 * Where this is used most often is for {@link PropertyModifyContext} and
 * {@link ActionArgValidityContext}, where a {@link Facet} (such as
 * <tt>MandatoryFacet</tt> or <tt>MaxLengthFacet</tt>) would want to perform the
 * same checks on the {@link #getProposed() proposed} value/argument.
 */
public interface ProposedHolder {

    ManagedObject getProposed();
}
