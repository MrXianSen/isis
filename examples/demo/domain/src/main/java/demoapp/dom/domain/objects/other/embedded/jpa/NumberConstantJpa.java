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
package demoapp.dom.domain.objects.other.embedded.jpa;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import demoapp.dom.domain.objects.other.embedded.ComplexNumber;
import demoapp.dom.domain.objects.other.embedded.persistence.NumberConstantEntity;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "NumberConstantJpa"
)
@EntityListeners(IsisEntityListener.class)
@DomainObject(logicalTypeName = "demo.NumberConstantEntity")
@NoArgsConstructor
public class NumberConstantJpa
        extends NumberConstantEntity {

    // ...

//end::class[]
    @ObjectSupport public String title() {
        return getName();
    }

    @Override
    public ComplexNumber value() {
        return getNumber();
    }

    @Id
    @GeneratedValue
    private Long id;

//tag::class[]
    @javax.jdo.annotations.Column(allowsNull = "false")
    @Property
    @Getter @Setter
    private String name;

    @javax.persistence.Embedded
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private ComplexNumberJpa number;
}
//end::class[]
