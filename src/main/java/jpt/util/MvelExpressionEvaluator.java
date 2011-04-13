/**
 *  Java Page Templates
 *  Copyright (C) 2004-2011 Christopher M Rossi
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package jpt.util;

import org.mvel.CompileException;
import org.mvel.MVEL;
import org.mvel.PropertyAccessException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mvel Expression Evaluator
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class MvelExpressionEvaluator implements ExpressionEvaluator {
    private Map<String, Object> context;

    public MvelExpressionEvaluator () {
        this.context = new ContainsEveryKeyMap<String, Object>(
            new HashMap<String, Object>());
    }

    public Object eval (String expression) throws EvalException {
        try {
            Map<String, Object> tokens = new HashMap<String, Object>();
            Object result = MVEL.eval(expression, this.context, tokens);
            this.context.putAll(tokens);
            return result;
        }
        catch (PropertyAccessException e) {
            throw new EvalException("Error accessing property in expression " + expression, e);
        }
        catch (CompileException e) {
            throw new EvalException("Error compiling MVEL expression " + expression, e);
        }
    }

    public Object get (String token) {
        return this.context.get(token);
    }

    public void set (String token, Object value) {
        this.context.put(token, value);
    }

    public void unset (String token) {
        this.context.remove(token);
    }

    static class ContainsEveryKeyMap<K, V> implements Map<K, V> {
        private final Map<K, V> delegate;

        public ContainsEveryKeyMap (Map<K, V> delegate) {
            this.delegate = delegate;
        }

        public void clear () {
            this.delegate.clear();
        }

        public boolean containsKey (Object key) {
            return true;
        }

        public boolean containsValue (Object value) {
            return this.delegate.containsValue(value);
        }

        public Set<Map.Entry<K, V>> entrySet () {
            return this.delegate.entrySet();
        }

        public V get (Object key) {
            return this.delegate.get(key);
        }

        public boolean isEmpty () {
            return this.delegate.isEmpty();
        }

        public Set<K> keySet () {
            return this.delegate.keySet();
        }

        public V put (K key, V value) {
            return this.delegate.put(key, value);
        }

        public void putAll (Map<? extends K, ? extends V> t) {
            this.delegate.putAll(t);
        }

        public V remove (Object key) {
            return this.delegate.remove(key);
        }

        public int size () {
            return this.delegate.size();
        }

        public Collection<V> values () {
            return this.delegate.values();
        }

        @Override
        public String toString () {
            return "ContainsEveryKeyMap{" +
                "delegate=" + delegate +
                '}';
        }
    }

    @Override
    public String toString () {
        return "MvelExpressionEvaluator{" +
            "context=" + context +
            '}';
    }
}
