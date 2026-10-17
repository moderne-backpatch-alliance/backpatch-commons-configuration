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
import static org.junit.Assert.assertSame;

import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.junit.Test;

/**
 * Tests PropertyConverter.toIterator() with java.nio.file.Path values, with
 * the inputs of the 2.x tests for CONFIGURATION-840 and -857. Needs Java 7.
 */
public class TestPropertyConverterPath
{
    private static final int[] SIZES = {0, 2, 4, 8, 16};

    private static List<Object> flatten(Object value)
    {
        List<Object> result = new ArrayList<Object>();
        for (Iterator<?> it = PropertyConverter.toIterator(value, ','); it.hasNext();)
        {
            result.add(it.next());
        }
        return result;
    }

    private static void assertSingleValue(Path path)
    {
        List<Object> result = flatten(path);
        assertEquals(1, result.size());
        assertSame(path, result.get(0));

        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("foo", path);
        assertSame(path, config.getProperty("foo"));
    }

    @Test
    public void testCompress840Path()
    {
        assertSingleValue(FileSystems.getDefault().getPath("foo"));
        assertSingleValue(FileSystems.getDefault().getPath("foo", "bar"));
        assertSingleValue(FileSystems.getDefault().getPath("bar"));
    }

    @Test
    public void testCompress840PathQueue()
    {
        for (int size : SIZES)
        {
            PriorityQueue<Object> object = new PriorityQueue<Object>();
            for (int i = 0; i < size; i++)
            {
                object.add(FileSystems.getDefault().getPath("foo"));
                object.add(FileSystems.getDefault().getPath("foo", "bar"));
            }
            assertEquals(2 * size, flatten(object).size());
        }
    }

    @Test
    public void testCompress840PriorityQueue()
    {
        for (int size : SIZES)
        {
            PriorityQueue<Object> object = new PriorityQueue<Object>();
            for (int i = 0; i < size; i++)
            {
                object.add(FileSystems.getDefault().getPath("foo"));
            }
            assertEquals(size, flatten(object).size());
        }
    }

    @Test
    public void testCompress840BeanContextSupport()
    {
        BeanContextSupport bcs = new BeanContextSupport();
        Path bar = FileSystems.getDefault().getPath("bar");
        bcs.add(bar);
        assertEquals(Arrays.asList((Object) bar), flatten(bcs));
    }

    @Test
    public void testCompress840BeanContextServicesSupport()
    {
        BeanContextSupport bcs = new BeanContextSupport();
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        Path bar = FileSystems.getDefault().getPath("bar");
        bcs.add(bar);
        bcss.add(bcs);
        assertEquals(Arrays.asList((Object) bar), flatten(bcss));
    }

    @Test
    public void testListOfRepeatedPaths()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("list.paths", Arrays.asList(Paths.get("path1"), Paths.get("path2"), Paths.get("path1")));
        assertEquals(Arrays.asList(Paths.get("path1"), Paths.get("path2"), Paths.get("path1")),
                config.getList("list.paths"));
    }
}
