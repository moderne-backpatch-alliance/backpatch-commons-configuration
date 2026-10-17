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

import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests PropertyConverter.toIterator() with self-containing values, with the
 * inputs of the 2.x tests for CONFIGURATION-840, -841 and -857.
 *
 * @version $Id$
 */
public class TestPropertyConverterCycles extends TestCase
{
    private static final int[] SIZES = {0, 2, 4, 8, 16};

    private static List flatten(Object value)
    {
        List result = new ArrayList();
        for (Iterator it = PropertyConverter.toIterator(value, ','); it.hasNext();)
        {
            result.add(it.next());
        }
        return result;
    }

    public void testCompress840ArrayList()
    {
        for (int s = 0; s < SIZES.length; s++)
        {
            List object = new ArrayList();
            for (int i = 0; i < SIZES[s]; i++)
            {
                object.add(new Integer(i));
            }
            assertEquals(object, flatten(object));
        }
    }

    public void testCompress840ArrayListCycle()
    {
        for (int s = 0; s < SIZES.length; s++)
        {
            List object = new ArrayList();
            for (int i = 0; i < SIZES[s]; i++)
            {
                object.add(String.valueOf(i));
                object.add(object);
                object.add(new ArrayList(object));
            }
            // each step repeats the values so far in the copy: f(n) = 2 * (f(n - 1) + 1)
            assertEquals("size " + SIZES[s], (1 << (SIZES[s] + 1)) - 2, flatten(object).size());
            object.add(object);
            flatten(object);
        }
    }

    public void testArrayListContainingItself()
    {
        List list = new ArrayList();
        list.add(new Integer(1));
        list.add(list);
        assertEquals(Arrays.asList(new Object[] {new Integer(1)}), flatten(list));

        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("foo", list);
        assertEquals(1, config.getInt("foo"));

        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        hc.addProperty("foo", list);
        assertEquals(1, hc.getInt("foo"));
    }

    public void testFlattenArrayCycle()
    {
        Object[] array = new Object[2];
        array[0] = "value1,value2";
        array[1] = array;
        assertEquals(Arrays.asList(new Object[] {"value1", "value2"}), flatten(array));
    }

    public void testFlattenMixedListAndArrayCycle()
    {
        List list = new ArrayList();
        Object[] array = {list};
        list.add("value1,value2");
        list.add(array);
        assertEquals(Arrays.asList(new Object[] {"value1", "value2"}), flatten(list));
    }

    public void testCompress840BeanContextSupport()
    {
        assertEquals(0, flatten(new BeanContextSupport()).size());
        assertEquals(0, flatten(new BeanContextSupport(new BeanContextSupport())).size());
        BeanContextSupport bcs = new BeanContextSupport();
        bcs.add(bcs);
        assertEquals(0, flatten(bcs).size());
    }

    public void testCompress840BeanContextServicesSupport()
    {
        assertEquals(0, flatten(new BeanContextServicesSupport()).size());
        BeanContextSupport bcs = new BeanContextSupport();
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        bcs.add("bar");
        bcss.add(bcs);
        bcss.add(bcss);
        assertEquals(Arrays.asList(new Object[] {"bar"}), flatten(bcss));
    }

    public void testRepeatedPlainValuesAndContainersAreKept()
    {
        List strings = Arrays.asList(new Object[] {"a", "b", "a"});
        List ints = Arrays.asList(new Object[] {new Integer(1), new Integer(2), new Integer(1)});
        List booleans = Arrays.asList(new Object[] {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE});
        BaseConfiguration config = new BaseConfiguration();
        config.addProperty("list.strings", strings);
        config.addProperty("list.ints", ints);
        config.addProperty("list.booleans", booleans);
        assertEquals(strings, config.getList("list.strings"));
        assertEquals(ints, config.getList("list.ints"));
        assertEquals(booleans, config.getList("list.booleans"));

        Integer value = new Integer(1000);
        List inner = Arrays.asList(new Object[] {value, value});
        List result = flatten(Arrays.asList(new Object[] {inner, inner}));
        assertEquals(4, result.size());
        for (Iterator it = result.iterator(); it.hasNext();)
        {
            assertSame(value, it.next());
        }
    }
}
