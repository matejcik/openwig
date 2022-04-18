/*
 Copyright (c) 2010 Kristofer Karlsson <kristofer.karlsson@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package se.krka.kahlua.j2se;

import se.krka.kahlua.vm.*;

import java.util.HashMap;
import java.util.Map;

public class KahluaTableImpl implements KahluaTable {
    protected final Map<Object, Object> delegate;
    protected KahluaTable metatable;

    public KahluaTableImpl() {
        this.delegate = new HashMap<>();
    }

    @Override
    public void setMetatable(KahluaTable metatable) {
        this.metatable = metatable;
    }

    @Override
    public KahluaTable getMetatable() {
        return metatable;
    }

    public Map<Object, Object> getDelegate() {
        return delegate;
    }

    @Override
    public void rawset(Object key, Object value) {
        if (value == null) {
            delegate.remove(key);
            return;
        }
        delegate.put(key, value);
    }

    @Override
    public Object rawget(Object key) {
        if (key == null) {
            return null;
        }
        return delegate.get(key);
    }

    @Override
    public void rawset(int key, Object value) {
        rawset(KahluaUtil.toDouble(key), value);
    }

    @Override
    public Object rawget(int key) {
        return rawget(KahluaUtil.toDouble(key));
    }

    @Override
    public int len() {
        return KahluaUtil.len(this, 0, 2 * delegate.size());
    }

    @Override
    public KahluaTableIterator iterator() {
        return new EntrySetIterator(delegate.entrySet().iterator());
    }

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public void wipe() {
		delegate.clear();
	}

	@Override
    public String toString() {
        return "table 0x" + System.identityHashCode(this);
    }
}
