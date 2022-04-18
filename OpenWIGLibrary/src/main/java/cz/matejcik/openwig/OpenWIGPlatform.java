package cz.matejcik.openwig;

import se.krka.kahlua.Version;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.j2se.MathLib;
import se.krka.kahlua.stdlib.*;
import se.krka.kahlua.vm.*;

public class OpenWIGPlatform implements Platform {
	private static OpenWIGPlatform INSTANCE = new OpenWIGPlatform();
	public static OpenWIGPlatform getInstance() {
		return INSTANCE;
	}
	
    @Override
    public double pow(double x, double y) {
        return Math.pow(x, y);
    }

    @Override
    public KahluaTable newTable() {
        return new KahluaTableImpl();
    }

    @Override
    public KahluaTable newEnvironment() {
        KahluaTable env = newTable();
		setupEnvironment(env);
        return env;
    }

	@Override
	public void setupEnvironment(KahluaTable env) {
		env.wipe();
		env.rawset("_G", env);
		env.rawset("_VERSION", Version.VERSION + " (J2SE)");

		MathLib.register(this, env);
		BaseLib.register(env);
		RandomLib.register(this, env);
		StringLib.register(this, env);
		CoroutineLib.register(this, env);
		OsLib.register(this, env);
		TableLib.register(this, env);
		WherigoLib.register(this, env);

		KahluaThread workerThread = KahluaUtil.getWorkerThread(this, env);
		KahluaUtil.setupLibrary(env, workerThread, "/stdlib");
	}
}
