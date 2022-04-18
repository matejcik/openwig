package cz.matejcik.openwig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.vm.*;

public class Cartridge extends EventTable {
	public List<Zone> zones = new ArrayList<>();
	public List<Timer> timers = new ArrayList<>();
	
	public List<Thing> things = new ArrayList<>();
	public List<Action> universalActions = new ArrayList<>();
	
	public List<Task> tasks = new ArrayList<>();
	
	public KahluaList allZObjects = new KahluaList();
	
	private static JavaFunction requestSync = new JavaFunction() {
		public int call (LuaCallFrame callFrame, int nArguments) {
			Engine.instance.store();
			return 0;
		}
	};

	public static void register () {
		Engine.instance.savegame.addJavafunc(requestSync);
	}

	protected String luaTostring () { return "a ZCartridge instance"; }
	
	public Cartridge () {
		delegate.put("RequestSync", requestSync);
		delegate.put("AllZObjects", allZObjects);
		allZObjects.getDelegate().add(this);
	}
		
	public void walk (ZonePoint zp) {	
		for (Zone z : zones) {
			z.walk(zp);
		}
	}
	
	public void tick () {
		for (Zone z : zones) {
			z.tick();
		}
		for (Timer t : timers) {
			t.updateRemaining();
		}
	}
	
	public int visibleZones () {
		int count = 0;
		for (Zone z : zones) {
			if (z.isVisible()) count++;
		}
		return count;
	}
	
	public int visibleThings () {
		int count = 0;
		for (Zone z : zones) {
			count += z.visibleThings();
		}
		return count;
	}
	
	public List<Thing> currentThings () {
		List<Thing> things = new ArrayList<>();
		for (Zone z : zones) {
			z.collectThings(things);
		}
		return things;
	}
	
	public int visibleUniversalActions () {
		int count = 0;
		for (Action a : universalActions) {
			if (a.isEnabled() && a.getActor().visibleToPlayer()) count++;
		}
		return count;
	}
	
	public int visibleTasks () {
		int count = 0;
		for (Task t : tasks) {
			if (t.isVisible()) count++;
		}
		return count;
	}
	
	public void addObject (Object o) {
		allZObjects.getDelegate().add(o);
		sortObject(o);
	}

	private void sortObject (Object o) {
		if (o instanceof Task) tasks.add((Task)o);
		else if (o instanceof Zone) zones.add((Zone)o);
		else if (o instanceof Timer) timers.add((Timer)o);
		else if (o instanceof Thing) things.add((Thing)o);
	}

	public void deserialize (DataInputStream in)
	throws IOException {
		super.deserialize(in);
		Engine.instance.cartridge = this;
		allZObjects = (KahluaList)delegate.get("AllZObjects");
		for (var entry: allZObjects.getDelegate()) {
			sortObject(entry);
		}
	}
}
