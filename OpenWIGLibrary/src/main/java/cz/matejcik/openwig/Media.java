package cz.matejcik.openwig;

import java.io.*;
import se.krka.kahlua.vm.*;

public class Media extends EventTable {
	
	private static int media_no;

	public static void reset () {
		media_no = 1;
	}
	
	public int id;
	public String altText = null;
	public String type = null;
	
	public Media() {
		id = media_no++;
	}

	public void serialize (DataOutputStream out) throws IOException {
		out.writeInt(id);
		super.serialize(out);
	}

	public void deserialize (DataInputStream in) throws IOException {
		media_no--; // deserialize must be called directly after construction
		id = in.readInt();
		if (id >= media_no) media_no = id + 1;
		super.deserialize(in);
	}
	
	protected void setItem (String key, Object value) {	
		if ("AltText".equals(key)) {
			altText = (String)value;
		} else if ("Resources".equals(key)) {
			LuaTable lt = (LuaTable)value;
			int n = lt.len();
			for (int i = 1; i <= n; i++) {
				LuaTable res = (LuaTable)lt.rawget(i);
				String t = (String)res.rawget("Type");
				if ("fdl".equals(t)) continue;
				type = t.toLowerCase();
			}
		} else super.setItem(key, value);
	}
	
	public String jarFilename () {
		return String.valueOf(id)+"."+(type==null ? "" : type);
	}
	
	public void play () {
		try {
			String mime = null;
			if ("wav".equals(type)) mime = "audio/x-wav";
			else if ("mp3".equals(type)) mime = "audio/mpeg";
			Engine.ui.playSound(Engine.mediaFile(this), mime);
		} catch (IOException e) {
			// meh
		}
	}
}
