/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.File;
import java.io.StringReader;

import junit.framework.TestCase;

/**
 * Tests cyclic includes in PropertiesConfiguration, with the input files of
 * the 2.x tests for CONFIGURATION-760.
 *
 * @version $Id$
 */
public class TestPropertiesConfigurationIncludeCycle extends TestCase
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

    public void testIncludeLoadCyclicalReferenceFail()
    {
        assertCycleDetected("src/test/resources/include-cyclical-reference.properties");
    }

    public void testIncludeLoadCyclicalMultiStepReferenceFail()
    {
        assertCycleDetected("src/test/resources/include-cyclical-root.properties");
    }

    public void testIncludeIncludeLoadCyclicalReferenceFail()
    {
        assertCycleDetected("src/test/resources/include-include-cyclical-reference.properties");
    }

    public void testCycleDetectedLoadingInConstructor()
    {
        try
        {
            new PropertiesConfiguration(new File("src/test/resources/include-cyclical-self.properties"));
            fail("Expected a ConfigurationException");
        }
        catch (ConfigurationException e)
        {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Cycle detected loading "));
        }
    }

    public void testAcyclicIncludeLoadedInConstructor() throws ConfigurationException
    {
        PropertiesConfiguration pc = new PropertiesConfiguration(new File("conf/test.properties"));
        assertEquals("true", pc.getString("include.loaded"));
    }

    public void testSameFileIncludedTwiceIsNoCycle() throws ConfigurationException
    {
        PropertiesConfiguration pc = new PropertiesConfiguration();
        pc.setBasePath(new File("conf").getAbsolutePath());
        pc.load(new StringReader("include = testEqual.properties, testEqual.properties\n"));
        assertEquals(2, pc.getList("property.c").size());
    }
}
