package cz.matejcik.openwig;

import java.io.DataInputStream;
import java.io.IOException;

import se.krka.kahlua.vm.*;

public class Container extends EventTable {

	public KahluaList inventory = new KahluaList();
	public Container container = null;
	
	private static JavaFunction moveTo = new JavaFunction() {
		public int call (LuaCallFrame callFrame, int nArguments) {
			Container subject = (Container) callFrame.get(0);
			Container target = (Container) callFrame.get(1);
			subject.moveTo(target);
			return 0;
		}
	};
	
	private static JavaFunction contains = new JavaFunction() {
		public int call (LuaCallFrame callFrame, int nArguments) {
			Container p = (Container) callFrame.get(0);
			Thing t = (Thing) callFrame.get(1);
			callFrame.push(KahluaUtil.toBoolean(p.contains(t)));
			return 1;
		}
	};

	public static void register () {
		Engine.instance.savegame.addJavafunc(moveTo);
		Engine.instance.savegame.addJavafunc(contains);
	}
	
	public Container() {
		delegate.put("MoveTo", moveTo);
		delegate.put("Contains", contains);
		delegate.put("Inventory", inventory);
		delegate.put("Container", container); // fix issues 181, 191
	}
	
	public void moveTo(Container c) {
		String cn = c == null ? "(nowhere)" : c.name;
		Engine.log("MOVE: "+name+" to "+cn, Engine.LOG_CALL);
		if (container != null) container.inventory.getDelegate().remove(this);
		// location.things.removeElement(this);
		if (c != null) {
			c.inventory.getDelegate().add(this);
			if (c == Engine.instance.player) setPosition(null);
			else if (position != null) setPosition(c.position);
			else if (container == Engine.instance.player) setPosition(ZonePoint.copy(Engine.instance.player.position));
			container = c;
		} else {
			container = null;
			delegate.put("ObjectLocation", null);
		}
		delegate.put("Container", container); // fix issues 181, 191
	}

	public boolean contains (Thing t) {
		return inventory.getDelegate().contains(t);
	}
	
	public boolean visibleToPlayer () {
		if (!isVisible()) return false;
		if (container == Engine.instance.player) return true;
		if (container instanceof Zone) {
			Zone z = (Zone)container;
			return z.showThings();
		}
		return false;
	}
	
	@Override
	public Object getItem (String key) {
		if ("Container".equals(key)) return container;
		else return super.getItem(key);
	}

	public void deserialize (DataInputStream in)
	throws IOException {
		super.deserialize(in);
		inventory = (KahluaList)delegate.get("Inventory");
		Object o = delegate.get("Container");
		if (o instanceof Container) container = (Container)o;
		else container = null;
	}
}
