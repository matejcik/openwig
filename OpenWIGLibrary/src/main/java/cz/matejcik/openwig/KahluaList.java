package cz.matejcik.openwig;

import java.util.ArrayList;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;

public class KahluaList implements KahluaTable {

    private final List<Object> delegate = new ArrayList<Object>();

    public List<Object> getDelegate() {
        return delegate;
    }

	public int len() {
        return delegate.size();
	}

    public KahluaTableIterator iterator() {
        return new KahluaTableIterator() {
            private Double curKey;
            private Object curValue;
            private int index = 1;

            public int call(LuaCallFrame callFrame, int nArguments) {
                if (advance()) {
                    return callFrame.push(getKey(), getValue());
                }
                return 0;
            }

            public boolean advance() {
                while (true) {
                    if (index > len()) {
                        return false;
                    }
                    Object value = rawget(index);
                    if (value != null) {
                        curKey = KahluaUtil.toDouble(index);
                        curValue = value;
                        index++;
                        return true;
                    }
                    index++;
                }
            }

            public Object getKey() {
                return curKey;
            }

            public Object getValue() {
                return curValue;
            }
        };
    }

	public Object rawget(int index) {
        if (index < 1 || index > delegate.size()) {
            return null;
        }
        return delegate.get(index - 1);
    }

    public void rawset(int index, Object value) {
        if (index <= 0 || index > delegate.size() + 1) {
            KahluaUtil.fail("Index out of range: " + index);
        }
        if (index == delegate.size() + 1 && value != null) {
            delegate.add(value);
        } else {
            delegate.set(index - 1, value);
        }
        if (value == null) {
            for (int i = delegate.size(); i > index; i--) {
                delegate.remove(i - 1);
            }
        }
    }

    private int getKeyIndex(Object key) {
        if (key instanceof Double) {
            Double d = (Double) key;
            return d.intValue();
        }
        return -1;
    }

    public Object rawget(Object key) {
        int index = getKeyIndex(key);
        return rawget(index);
    }

    public void rawset(Object key, Object value) {
        int index = getKeyIndex(key);
        if (index == -1) {
            KahluaUtil.fail("Invalid table key: " + key);
        }
        rawset(index, value);
    }

    public KahluaTable getMetatable() {
		return null;
	}

	public void setMetatable(KahluaTable metatable) {
        KahluaUtil.fail("Cannot set metatable on list of items");
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void wipe() {
        delegate.clear();
    }
}
