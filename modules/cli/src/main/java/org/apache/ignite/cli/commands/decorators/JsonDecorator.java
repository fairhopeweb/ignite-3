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

package org.apache.ignite.cli.commands.decorators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ignite.cli.commands.decorators.core.Decorator;
import org.apache.ignite.cli.commands.decorators.core.TerminalOutput;

/**
 * Pretty json decorator.
 */
public class JsonDecorator implements Decorator<String, TerminalOutput> {

    /** {@inheritDoc} */
    @Override
    public TerminalOutput decorate(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        return () -> {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(jsonString, JsonNode.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}