package cz.matejcik.openwig;

import java.io.DataInputStream;
import java.io.IOException;

import se.krka.kahlua.vm.*;

public class Player extends Thing {

	private KahluaList insideOfZones = new KahluaList();
	
	private static JavaFunction refreshLocation = new JavaFunction() {
		public int call (LuaCallFrame callFrame, int nArguments) {
			Engine.instance.player.refreshLocation();
			return 0;
		}
	};

	public static void register () {
		Engine.instance.savegame.addJavafunc(refreshLocation);
	}
	
	public Player() {
		super(true);
		delegate.put("RefreshLocation", refreshLocation);
		delegate.put("InsideOfZones", insideOfZones);
		setPosition(new ZonePoint(360,360,0));
	}

	public void moveTo (Container c) {
		// do nothing
	}

	public void enterZone (Zone z) {
		container = z;
		if (!insideOfZones.getDelegate().contains(z)) {
			insideOfZones.getDelegate().add(z);
		}
	}

	public void leaveZone (Zone z) {
		insideOfZones.getDelegate().remove(z);
		if (insideOfZones.len() > 0)
			container = (Container)insideOfZones.rawget(insideOfZones.len());
	}

	protected String luaTostring () { return "a Player instance"; }

	public void deserialize (DataInputStream in)
	throws IOException {
		super.deserialize(in);
		Engine.instance.player = this;
		//setPosition(new ZonePoint(360,360,0));
	}
	
	public int visibleThings() {
		int count = 0;
		for (var value: inventory.getDelegate()) {
			if (value instanceof Thing && ((Thing)value).isVisible()) count++;
		}
		return count;
	}

	public void refreshLocation() {
		position.latitude = Engine.gps.getLatitude();
		position.longitude = Engine.gps.getLongitude();
		position.altitude = Engine.gps.getAltitude();
		delegate.put("PositionAccuracy", KahluaUtil.toDouble(Engine.gps.getPrecision()));
		Engine.instance.cartridge.walk(position);
	}

	@Override
	public void setItem (String key, Object value) {
		if ("ObjectLocation".equals(key)) return;
		super.setItem(key, value);
	}

	public Object rawget (Object key) {
		if ("ObjectLocation".equals(key)) return ZonePoint.copy(position);
		return super.rawget(key);
	}
}
