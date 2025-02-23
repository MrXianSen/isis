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
package org.apache.isis.core.metamodel.facets.object.callback;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.CallbackMethod;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;

public class RemovingCallbackFacetFactoryTest
extends CallbackFacetFactoryTestAbstract {

    public void testRemovingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void removing() {
            }
        }
        assertPicksUp(1, facetFactory, Customer.class, CallbackMethod.REMOVING, RemovingCallbackFacet.class);
    }

    public void testDeletingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void deleting() {
            }
        }
        assertPicksUp(1, facetFactory, Customer.class, CallbackMethod.REMOVING, RemovingCallbackFacet.class);
    }

    public void testRemovingAndDeletingLifecycleMethodPickedUpOn() {
        class Customer {
            @SuppressWarnings("unused")
            public void removing() {
            }
            @SuppressWarnings("unused")
            public void deleting() {
            }
        }
        assertPicksUp(2, facetFactory, Customer.class, CallbackMethod.REMOVING, RemovingCallbackFacet.class);
    }

}
