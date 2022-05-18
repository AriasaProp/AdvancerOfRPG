package com.ariasaproject.advancerofrpg;

public class OSInitializer {

	public static void init() {
		boolean succes = false;
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win"))
			succes = init_Windows();
		else if (osName.contains("mac"))
			succes = init_Mac();
		else if (osName.contains("unix") || osName.contains("nux"))
			succes = init_Linux();
		else if (osName.contains("android"))
			succes = init_Android();
		if (!succes) {
			System.out.println("failed to load library : " + osName);
			System.err.println("failed load library");
			System.exit(0);
		}
	}

	private static boolean init_Windows() {
		System.loadLibrary("ext");
		return true;
	}

	private static boolean init_Mac() {
		System.loadLibrary("ext");
		return true;
	}

	private static boolean init_Linux() {
		System.loadLibrary("ext");
		return true;
	}

	private static boolean init_Android() {
		System.loadLibrary("ext");
		return true;
	}

}
