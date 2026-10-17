/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

/**
 * Tests that long keys are parsed by PropertiesConfiguration, with the input
 * of the 2.x test for CONFIGURATION-838.
 */
public class TestPropertiesConfigurationLargeKey
{
    private static String repeat(String s, int count)
    {
        StringBuilder buf = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++)
        {
            buf.append(s);
        }
        return buf.toString();
    }

    private static PropertiesConfiguration load(String content) throws ConfigurationException
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.load(new StringReader(content));
        return pc;
    }

    @Test
    public void testLargeKeyWithoutValue() throws ConfigurationException
    {
        PropertiesConfiguration pc = load(repeat("x", 10000) + "\nafter = ok\n");
        assertEquals("ok", pc.getString("after"));
    }

    @Test
    public void testLargeKey() throws ConfigurationException
    {
        String key = repeat("x", 10000);
        PropertiesConfiguration pc = load(key + " = value\n");
        assertEquals("value", pc.getString(key));
    }

    @Test
    public void testLargeKeyWithEscapedSeparators() throws ConfigurationException
    {
        PropertiesConfiguration pc = load(repeat("a\\=", 5000) + ":value\n");
        assertEquals("value", pc.getString(repeat("a=", 5000)));
    }

    @Test
    public void testShortKeysUnchanged() throws ConfigurationException
    {
        PropertiesConfiguration pc = load("a\\=b = 1\nc:2\nd 3\ne\\ f=4\n");
        assertEquals("1", pc.getString("a=b"));
        assertEquals("2", pc.getString("c"));
        assertEquals("3", pc.getString("d"));
        assertEquals("4", pc.getString("e f"));
    }
}
