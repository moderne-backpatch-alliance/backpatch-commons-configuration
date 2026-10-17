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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Tests PropertyConverter.toIterator() with self-containing values, with the
 * inputs of the 2.x tests for CONFIGURATION-840, -841 and -857.
 */
public class TestPropertyConverterCycles
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

    @Test
    public void testCompress840ArrayList()
    {
        for (int size : SIZES)
        {
            List<Object> object = new ArrayList<Object>();
            for (int i = 0; i < size; i++)
            {
                object.add(i);
            }
            assertEquals(object, flatten(object));
        }
    }

    @Test
    public void testCompress840ArrayListCycle()
    {
        for (int size : SIZES)
        {
            List<Object> object = new ArrayList<Object>();
            for (int i = 0; i < size; i++)
            {
                object.add(String.valueOf(i));
                object.add(object);
                object.add(new ArrayList<Object>(object));
            }
            // each step repeats the values so far in the copy: f(n) = 2 * (f(n - 1) + 1)
            assertEquals("size " + size, (1 << (size + 1)) - 2, flatten(object).size());
            object.add(object);
            flatten(object);
        }
    }

    @Test
    public void testArrayListContainingItself()
    {
        List<Object> list = new ArrayList<Object>();
        list.add(1);
        list.add(list);
        assertEquals(Arrays.asList((Object) 1), flatten(list));

        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("foo", list);
        assertEquals(1, config.getInt("foo"));

        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        hc.addProperty("foo", list);
        assertEquals(1, hc.getInt("foo"));
    }

    @Test
    public void testFlattenArrayCycle()
    {
        Object[] array = new Object[2];
        array[0] = "value1,value2";
        array[1] = array;
        assertEquals(Arrays.asList("value1", "value2"), flatten(array));
    }

    @Test
    public void testFlattenMixedListAndArrayCycle()
    {
        List<Object> list = new ArrayList<Object>();
        Object[] array = {list};
        list.add("value1,value2");
        list.add(array);
        assertEquals(Arrays.asList("value1", "value2"), flatten(list));
    }

    @Test
    public void testCompress840BeanContextSupport()
    {
        assertEquals(0, flatten(new BeanContextSupport()).size());
        assertEquals(0, flatten(new BeanContextSupport(new BeanContextSupport())).size());
        BeanContextSupport bcs = new BeanContextSupport();
        bcs.add(bcs);
        assertEquals(0, flatten(bcs).size());
    }

    @Test
    public void testCompress840BeanContextServicesSupport()
    {
        assertEquals(0, flatten(new BeanContextServicesSupport()).size());
        BeanContextSupport bcs = new BeanContextSupport();
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        bcs.add("bar");
        bcss.add(bcs);
        bcss.add(bcss);
        assertEquals(Arrays.asList((Object) "bar"), flatten(bcss));
    }

    @Test
    public void testRepeatedPlainValuesAndContainersAreKept()
    {
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("list.strings", Arrays.asList("a", "b", "a"));
        config.addProperty("list.ints", Arrays.asList(1, 2, 1));
        config.addProperty("list.booleans", Arrays.asList(true, false, true));
        assertEquals(Arrays.asList("a", "b", "a"), config.getList("list.strings"));
        assertEquals(Arrays.asList((Object) 1, 2, 1), config.getList("list.ints"));
        assertEquals(Arrays.asList((Object) true, false, true), config.getList("list.booleans"));

        Integer value = Integer.valueOf(1000);
        List<Object> inner = Arrays.asList((Object) value, value);
        List<Object> result = flatten(Arrays.asList(inner, inner));
        assertEquals(4, result.size());
        for (Object o : result)
        {
            assertSame(value, o);
        }
    }
}
