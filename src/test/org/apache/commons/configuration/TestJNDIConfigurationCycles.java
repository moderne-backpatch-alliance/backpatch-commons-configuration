/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import junit.framework.TestCase;

/**
 * Tests JNDIConfiguration.getKeys() on JNDI trees with cycles
 * (CONFIGURATION-281), on a small in-memory context.
 *
 * @version $Id$
 */
public class TestJNDIConfigurationCycles extends TestCase
{
    private static Set keys(JNDIConfiguration conf, String prefix)
    {
        Set result = new HashSet();
        for (Iterator it = conf.getKeys(prefix); it.hasNext();)
        {
            result.add(it.next());
        }
        return result;
    }

    private static Set set(String[] values)
    {
        return new HashSet(Arrays.asList(values));
    }

    /** The tree of upstream's testGetKeysWithCycles: a context that contains itself. */
    public void testGetKeysWithCycles()
    {
        TreeContext root = new TreeContext();
        TreeContext test = new TreeContext();
        TreeContext cycle = new TreeContext();
        root.put("test", test);
        root.put("cycle", cycle);
        test.put("key1", "value1");
        cycle.put("key1", "value1");
        cycle.put("key2", "value2");
        cycle.put("cycleCtx", cycle);

        JNDIConfiguration conf = new JNDIConfiguration(root);
        assertEquals(set(new String[] {"cycle.key1", "cycle.key2"}), keys(conf, "cycle"));
        assertEquals(set(new String[] {"test.key1", "cycle.key1", "cycle.key2"}), keys(conf, ""));
    }

    public void testGetKeysWithTwoStepCycle()
    {
        TreeContext root = new TreeContext();
        TreeContext a = new TreeContext();
        TreeContext b = new TreeContext();
        root.put("a", a);
        a.put("x", "1");
        a.put("b", b);
        b.put("y", "2");
        b.put("a", a);

        JNDIConfiguration conf = new JNDIConfiguration(root);
        assertEquals(set(new String[] {"a.x", "a.b.y"}), keys(conf, ""));
    }

    public void testGetKeysWithoutCycles()
    {
        TreeContext root = new TreeContext();
        TreeContext test = new TreeContext();
        TreeContext sub = new TreeContext();
        root.put("test", test);
        root.put("other", "value");
        test.put("key1", "value1");
        test.put("sub", sub);
        sub.put("key2", "value2");

        JNDIConfiguration conf = new JNDIConfiguration(root);
        assertEquals(set(new String[] {"test.key1", "test.sub.key2", "other"}), keys(conf, ""));
        assertEquals(set(new String[] {"test.key1", "test.sub.key2"}), keys(conf, "test"));
        assertEquals("value2", conf.getProperty("test.sub.key2"));
    }

    /**
     * A read-only context over its own bindings: list("") and lookup(String),
     * where "" is the context itself and "a/b" looks up b in the context a.
     */
    private static class TreeContext implements Context
    {
        private final Map bindings = new HashMap();

        void put(String name, Object value)
        {
            bindings.put(name, value);
        }

        public Object lookup(String name) throws NamingException
        {
            if (name.length() == 0)
            {
                return this;
            }
            int slash = name.indexOf('/');
            String first = slash < 0 ? name : name.substring(0, slash);
            Object value = bindings.get(first);
            if (value == null)
            {
                throw new NameNotFoundException(name);
            }
            if (slash < 0)
            {
                return value;
            }
            if (!(value instanceof Context))
            {
                throw new NameNotFoundException(name);
            }
            return ((Context) value).lookup(name.substring(slash + 1));
        }

        public NamingEnumeration list(String name) throws NamingException
        {
            if (name.length() > 0)
            {
                return ((Context) lookup(name)).list("");
            }
            final Iterator it = bindings.entrySet().iterator();
            return new NamingEnumeration()
            {
                public boolean hasMore()
                {
                    return it.hasNext();
                }

                public Object next()
                {
                    Map.Entry e = (Map.Entry) it.next();
                    return new NameClassPair((String) e.getKey(), e.getValue().getClass().getName());
                }

                public boolean hasMoreElements()
                {
                    return hasMore();
                }

                public Object nextElement()
                {
                    return next();
                }

                public void close()
                {
                }
            };
        }

        public Object lookup(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void bind(Name name, Object obj) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void bind(String name, Object obj) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void rebind(Name name, Object obj) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void rebind(String name, Object obj) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void unbind(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void unbind(String name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void rename(Name oldName, Name newName) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void rename(String oldName, String newName) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public NamingEnumeration list(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public NamingEnumeration listBindings(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public NamingEnumeration listBindings(String name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void destroySubcontext(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void destroySubcontext(String name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Context createSubcontext(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Context createSubcontext(String name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Object lookupLink(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Object lookupLink(String name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public NameParser getNameParser(Name name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public NameParser getNameParser(String name) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Name composeName(Name name, Name prefix) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public String composeName(String name, String prefix) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Object addToEnvironment(String propName, Object propVal) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Object removeFromEnvironment(String propName) throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public Hashtable getEnvironment() throws NamingException
        {
            throw new OperationNotSupportedException();
        }

        public void close()
        {
        }

        public String getNameInNamespace() throws NamingException
        {
            throw new OperationNotSupportedException();
        }
    }
}
