package se.krka.kahlua.j2se;

import java.util.Iterator;
import java.util.Map;

import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.LuaCallFrame;

public class EntrySetIterator implements KahluaTableIterator {
    private final Iterator<Map.Entry<Object, Object>> iterator;
    private Map.Entry<Object, Object> current;

    public EntrySetIterator(Iterator<Map.Entry<Object, Object>> iterator) {
        this.iterator = iterator;
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        if (advance()) {
            return callFrame.push(getKey(), getValue());
        }
        return 0;
    }

    @Override
    public boolean advance() {
        if (iterator.hasNext()) {
            current = iterator.next();
            return true;
        }
        current = null;
        return false;
    }

    @Override
    public Object getKey() {
        return current.getKey();

    }

    @Override
    public Object getValue() {
        return current.getValue();
    }
}
