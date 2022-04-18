package cz.matejcik.openwig;

import java.io.*;
import se.krka.kahlua.vm.*;

import java.util.ArrayList;
import java.util.List;

import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.stdlib.BaseLib;

public class Thing extends Container {
	
	private boolean character = false;

	protected String luaTostring () { return character ? "a ZCharacter instance" : "a ZItem instance"; }
	
	public List<Action> actions = new ArrayList<>();

	public Thing () {
		// for serialization
	}

	public void serialize (DataOutputStream out) throws IOException {
		out.writeBoolean(character);
		super.serialize(out);
	}

	public void deserialize (DataInputStream in) throws IOException {
		character = in.readBoolean();
		super.deserialize(in);
	}
	
	public Thing(boolean character) {
		this.character = character;
		delegate.put("Commands", new KahluaTableImpl());
	}
	
	protected void setItem (String key, Object value) {
		if ("Commands".equals(key)) {
			// clear out existing actions
			for (Action a : actions) {
				a.dissociateFromTargets();
			}
			actions.clear();

			// add new actions
			KahluaTable lt = (KahluaTable)value;
			var it = lt.iterator();
			while (it.advance()) {
				Object k = it.getKey();
				Action a = (Action)it.getValue();
				//a.name = (String)i;
				if (k instanceof Double) a.name = KahluaUtil.numberToString((Double)k);
				else a.name = k.toString();
				a.setActor(this);
				actions.add(a);
				a.associateWithTargets();
			}
		} else super.setItem(key, value);
	}
	
	public int visibleActions() {
		int count = 0;
		for (Action a : actions) {
			if (!a.isEnabled()) continue;
			if (a.getActor() == this || a.getActor().visibleToPlayer()) count++;
		}
		return count;
	}
	
	public boolean isItem() {
		return !character;
	}
	
	public boolean isCharacter() {
		return character;
	}
}
