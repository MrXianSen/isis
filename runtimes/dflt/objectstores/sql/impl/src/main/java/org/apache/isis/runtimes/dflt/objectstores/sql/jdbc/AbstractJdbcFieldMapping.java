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


package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;


public abstract class AbstractJdbcFieldMapping implements FieldMapping {
    private final String columnName;
    protected final ObjectAssociation field;
        
    public AbstractJdbcFieldMapping(ObjectAssociation field) {
        this.field = field;
        columnName = Sql.sqlFieldName(field.getId());
    }

    @Override
    public void appendColumnDefinitions(StringBuffer sql) {
        sql.append(columnName);
        sql.append(" ");
        sql.append(columnType());
    }

    @Override
    public void appendColumnNames(StringBuffer sql) {
        sql.append(columnName);
    }

    @Override
    public void appendInsertValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        ObjectAdapter fieldValue = field.get(object);
        if (fieldValue == null) {
            sql.append("NULL");
        } else {
        	sql.append("?");
            connector.addToQueryValues(preparedStatementObject(fieldValue));
        }
    }

    @Override
    public void appendUpdateValues(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        appendEqualsClause(connector, sql, object, "=");
    }

    @Override
    public void appendWhereClause(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object) {
        appendEqualsClause(connector, sql, object, "=");
    }

    protected void appendEqualsClause(DatabaseConnector connector, StringBuffer sql, ObjectAdapter object,
        String condition) {
        sql.append(Sql.sqlFieldName(field.getId()));
        sql.append(condition);
        ObjectAdapter fieldValue = field.get(object);
        sql.append("?");
        connector.addToQueryValues(preparedStatementObject(fieldValue));
    }
    
    @Override
    public void initializeField(ObjectAdapter object, Results rs) {
        String columnName = Sql.sqlFieldName(field.getId());
        String encodedValue = rs.getString(columnName);
        ObjectAdapter restoredValue;
        if (encodedValue == null) {
            restoredValue = null;
        } else {
            restoredValue = setFromDBColumn(rs, encodedValue, columnName, field);
            
        }
        ((OneToOneAssociation) field).initAssociation(object, restoredValue);
    }
    
    @Override
    public void debugData(DebugBuilder debug) {
        debug.appendln(field.getId(), columnName + "/" + columnType());
    }

    protected abstract String columnType();

    protected abstract Object preparedStatementObject(ObjectAdapter value);

    protected abstract ObjectAdapter setFromDBColumn(Results results, String encodeValue, String columnName,
        ObjectAssociation field);


}

