package com.ariasaproject.advancerofrpg;

import java.io.Serializable;

import com.ariasaproject.advancerofrpg.graphics.TGF;

public class GraphFunc implements Serializable {
	private static final long serialVersionUID = -6463386136171354152L;

	public static Application app;
	public static TGF tgf;

	public static TGF getTGF() {
		return GraphFunc.tgf;
	}

	public static native String nativeLog();
}
