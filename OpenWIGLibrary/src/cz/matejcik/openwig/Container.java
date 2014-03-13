package cz.matejcik.openwig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.vm.*;

public class Container extends EventTable {

	public LuaTable inventory = new LuaTableImpl();
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
			callFrame.push(LuaState.toBoolean(p.contains(t)));
			return 1;
		}
	};

	public static void register () {
		Engine.instance.savegame.addJavafunc(moveTo);
		Engine.instance.savegame.addJavafunc(contains);
	}
	
	public Container() {
		table.rawset("MoveTo", moveTo);
		table.rawset("Contains", contains);
		table.rawset("Inventory", inventory);
	}
	
	public void moveTo(Container c) {
		String cn = c == null ? "(nowhere)" : c.name;
		Engine.log("MOVE: "+name+" to "+cn, Engine.LOG_CALL);
		if (container != null) TableLib.removeItem(container.inventory, this);
		// location.things.removeElement(this);
		if (c != null) {
			TableLib.rawappend(c.inventory, this);
			if (c == Engine.instance.player) setPosition(null);
			else if (position != null) setPosition(c.position);
			else if (container == Engine.instance.player) setPosition(ZonePoint.copy(Engine.instance.player.position));
			container = c;
		} else {
			container = null;
			rawset("ObjectLocation", null);
		}
	}

	public boolean contains (Thing t) {
		Object key = null;
		while ((key = inventory.next(key)) != null) {
			Object value = inventory.rawget(key);
			if (value instanceof Thing) {
				if (value == t) return true;
				if (((Thing)value).contains(t)) return true;
			}
		}
		return false;
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
	
	public Object rawget (Object key) {
		if ("Container".equals(key)) return container;
		else return super.rawget(key);
	}
	
	public void serialize (DataOutputStream out)
	throws IOException {
		super.serialize(out);
		Engine.instance.savegame.storeValue(container, out);
	}

	public void deserialize (DataInputStream in)
	throws IOException {
		container = (Container)Engine.instance.savegame.restoreValue(in, null);
		super.deserialize(in);
		inventory = (LuaTable)table.rawget("Inventory");
	}
}
