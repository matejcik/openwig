package cz.matejcik.openwig;

import se.krka.kahlua.vm.*;

import java.util.ArrayList;
import java.util.List;

public class Action extends EventTable {
	
	private boolean parameter;
	private boolean reciprocal = true;
	private boolean enabled;

	private Thing actor = null;
	private List<Thing> targets = new ArrayList<>();
	private boolean universal;
	
	public String text;
	public String notarget;

	public Action () {
		// for serialization
	}
	
	public Action (KahluaTable table) {
		setTable(table);
	}

	public void associateWithTargets () {
		if (!hasParameter()) return;
		if (isReciprocal()) {
			for (Thing thing : targets) {
				if (!thing.actions.contains(this)) {
					thing.actions.add(this);
				}
			}
		}
		if (isUniversal() && !Engine.instance.cartridge.universalActions.contains(this)) {
			Engine.instance.cartridge.universalActions.add(this);
		}
	}

	public void dissociateFromTargets () {
		if (!hasParameter()) return;
		if (isReciprocal()) {
			for (Thing thing : targets) {
				thing.actions.remove(this);
			}
		}
		if (isUniversal()) {
			Engine.instance.cartridge.universalActions.remove(this);
		}
	}

	protected String luaTostring () { return "a ZCommand instance"; }
	
	protected void setItem (String key, Object value) {
		if ("Text".equals(key)) {
			text = (String)value;
		} else if ("CmdWith".equals(key)) {
			boolean np = KahluaUtil.boolEval(value);
			if (np != parameter) {
				if (np) {
					parameter = true;
					associateWithTargets();
				} else {
					dissociateFromTargets();
					parameter = false;
				}
			}
		} else if ("Enabled".equals(key)) {
			enabled = KahluaUtil.boolEval(value);
		} else if ("WorksWithAll".equals(key)) {
			// XXX bug: when the command is dissociated and somebody updates this, it will re-associate
			dissociateFromTargets();
			universal = KahluaUtil.boolEval(value);
			associateWithTargets();
		} else if ("WorksWithList".equals(key)) {
			dissociateFromTargets();
			KahluaTable lt = (KahluaTable)value;
			var iter = lt.iterator();
			while (iter.advance()) {
				targets.add((Thing)iter.getValue());
			}
			associateWithTargets();
		} else if ("MakeReciprocal".equals(key)) {
			dissociateFromTargets();
			reciprocal = KahluaUtil.boolEval(value);
			associateWithTargets();
		} else if ("EmptyTargetListText".equals(key)) {
			notarget = value == null ? "(not available now)" : value.toString();
		} else {
			super.setItem(key, value);
		}
	}
	
	public int visibleTargets(Container where) {
		int count = 0;
		for (var o: where.inventory.getDelegate()) {
			if (!(o instanceof Thing)) continue;
			Thing t = (Thing)o;
			if (t.isVisible() && (targets.contains(t) || isUniversal())) count++;
		}
		return count;
	}
	
	/*public int targetsInside(KahluaTable v) {
		int count = 0;
		Object key = null;
		while ((key = v.next(key)) != null) {
			Object o = v.rawget(key);
			if (!(o instanceof Thing)) continue;
			Thing t = (Thing)o;
			if (t.isVisible() && (targets.contains(t) || isUniversal())) count++;
		}
		return count;
	}*/
	
	public boolean isTarget(Thing t) {
		return targets.contains(t) || isUniversal();
	}

	public List<Thing> getTargets () {
		return targets;
	}

	public String getName() {
		return name;
	}

	public boolean hasParameter() {
		return parameter;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isUniversal() {
		return universal;
	}
	
	public void setActor (Thing a) {
		actor = a;
	}
	
	public Thing getActor () {
		return actor;
	}

	public boolean isReciprocal () {
		return reciprocal;
	}
}
