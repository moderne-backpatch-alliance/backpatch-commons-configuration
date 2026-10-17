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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringReader;

import org.junit.Test;

/**
 * Tests cyclic includes in PropertiesConfiguration, with the input files of
 * the 2.x tests for CONFIGURATION-760.
 */
public class TestPropertiesConfigurationIncludeCycle
{
    /** The include file names in the test files are relative to the project root. */
    private static final String BASE_PATH = new File("").getAbsolutePath();

    private static void assertCycleDetected(String fileName)
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(BASE_PATH);
        pc.setFileName(fileName);
        try
        {
            pc.load();
            fail("Expected a ConfigurationException loading " + fileName);
        }
        catch (ConfigurationException e)
        {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Cycle detected loading "));
        }
        assertNull(pc.getString("keyA"));
    }

    @Test
    public void testIncludeLoadCyclicalReferenceFail()
    {
        assertCycleDetected("src/test/resources/include-cyclical-reference.properties");
    }

    @Test
    public void testIncludeLoadCyclicalMultiStepReferenceFail()
    {
        assertCycleDetected("src/test/resources/include-cyclical-root.properties");
    }

    @Test
    public void testIncludeIncludeLoadCyclicalReferenceFail()
    {
        assertCycleDetected("src/test/resources/include-include-cyclical-reference.properties");
    }

    @Test
    public void testCycleDetectedLoadingInConstructor()
    {
        try
        {
            new PropertiesConfiguration(ConfigurationAssert.getTestFile("include-cyclical-self.properties"));
            fail("Expected a ConfigurationException");
        }
        catch (ConfigurationException e)
        {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Cycle detected loading "));
        }
    }

    @Test
    public void testAcyclicIncludeLoadedInConstructor() throws ConfigurationException
    {
        PropertiesConfiguration pc = new PropertiesConfiguration(ConfigurationAssert.getTestFile("test.properties"));
        assertEquals("true", pc.getString("include.loaded"));
    }

    @Test
    public void testSameFileIncludedTwiceIsNoCycle() throws ConfigurationException
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(ConfigurationAssert.TEST_DIR.getAbsolutePath());
        pc.load(new StringReader("include = testEqual.properties, testEqual.properties\n"));
        assertEquals(2, pc.getList("property.c").size());
    }
}
