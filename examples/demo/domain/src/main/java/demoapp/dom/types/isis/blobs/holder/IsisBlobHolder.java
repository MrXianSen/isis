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
package demoapp.dom.types.isis.blobs.holder;

import org.apache.isis.applib.annotation.LogicalTypeName;
import org.apache.isis.applib.value.Blob;

@LogicalTypeName("demo.IsisBlobHolder")
//tag::class[]
public interface IsisBlobHolder {

    Blob getReadOnlyProperty();
    void setReadOnlyProperty(Blob c);

    Blob getReadWriteProperty();
    void setReadWriteProperty(Blob c);

    Blob getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(Blob c);

    Blob getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(Blob c);

}
//end::class[]
