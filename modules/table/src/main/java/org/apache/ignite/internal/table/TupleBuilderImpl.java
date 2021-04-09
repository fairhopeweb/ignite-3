/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.table;

import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjects;
import org.apache.ignite.table.Tuple;
import org.apache.ignite.table.TupleBuilder;

/**
 * Buildable tuple.
 */
public class TupleBuilderImpl implements TupleBuilder, Tuple {
    /** Columns values. */
    private final Map<String, Object> map;

    /**
     * Constructor.
     */
    public TupleBuilderImpl() {
        map = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override public TupleBuilder set(String colName, Object value) {
        map.put(colName, value);

        return this;
    }

    /** {@inheritDoc} */
    @Override public Tuple build() {
        return this;
    }

    @Override public <T> T value(String colName) {
        return (T)map.get(colName);
    }

    /** {@inheritDoc} */
    @Override public BinaryObject binaryObjectField(String colName) {
        byte[] data = value(colName);

        return BinaryObjects.wrap(data);
    }

    /** {@inheritDoc} */
    @Override public byte byteValue(String colName) {
        return value(colName);
    }

    /** {@inheritDoc} */
    @Override public short shortValue(String colName) {
        return value(colName);
    }

    /** {@inheritDoc} */
    @Override public int intValue(String colName) {
        return value(colName);
    }

    /** {@inheritDoc} */
    @Override public long longValue(String colName) {
        return value(colName);
    }

    /** {@inheritDoc} */
    @Override public float floatValue(String colName) {
        return value(colName);
    }

    /** {@inheritDoc} */
    @Override public double doubleValue(String colName) {
        return value(colName);
    }

    /** {@inheritDoc} */
    @Override public String stringValue(String colName) {
        return value(colName);
    }
}
